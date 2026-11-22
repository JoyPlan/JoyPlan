package com.joyplan.controller;

import com.joyplan.dto.OutingForm;
import com.joyplan.model.Plan;
import com.joyplan.model.User;
import com.joyplan.service.FamilyService;
import com.joyplan.service.PlanService;
import com.joyplan.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/plans")
public class PlanController {

    private final UserService userService;
    private final FamilyService familyService;
    private final PlanService planService;

    public PlanController(UserService userService, FamilyService familyService, PlanService planService) {
        this.userService = userService;
        this.familyService = familyService;
        this.planService = planService;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        User user = userService.current(principal);
        model.addAttribute("plans", planService.plansOf(user));
        model.addAttribute("sharedPlans", planService.sharedWith(user));
        return "plans";
    }

    @GetMapping("/new")
    public String newPlan(Principal principal, Model model, RedirectAttributes redirect) {
        User user = userService.current(principal);
        if (familyService.children(user).isEmpty()) {
            redirect.addFlashAttribute("error", "Please add at least one child to your family profile first.");
            return "redirect:/family";
        }
        OutingForm form = new OutingForm();
        form.setArea(user.getHomeArea());
        model.addAttribute("form", form);
        model.addAttribute("children", familyService.children(user));
        return "plan-new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") OutingForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes redirect) {
        User user = userService.current(principal);
        if (result.hasErrors()) {
            model.addAttribute("children", familyService.children(user));
            return "plan-new";
        }
        try {
            Plan plan = planService.generate(user, form.toRequest());
            if (!plan.getItems().isEmpty()) {
                redirect.addFlashAttribute("success", "Your JoyPlan is ready. Review it, swap any stop, then save it.");
            }
            return "redirect:/plans/" + plan.getId();
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/family";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Principal principal, Model model) {
        User user = userService.current(principal);
        Plan plan = planService.accessible(id, user);
        model.addAttribute("plan", plan);
        model.addAttribute("isOwner", plan.isOwnedBy(user));
        return "plan-view";
    }

    @PostMapping("/{id}/regenerate")
    public String regenerate(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        planService.regenerate(id, userService.current(principal));
        redirect.addFlashAttribute("success", "A new plan was generated with different stops where possible.");
        return "redirect:/plans/" + id;
    }

    @GetMapping("/{id}/items/{itemId}/alternatives")
    public String alternatives(@PathVariable Long id, @PathVariable Long itemId, Principal principal, Model model) {
        User user = userService.current(principal);
        model.addAttribute("plan", planService.accessible(id, user));
        model.addAttribute("alternatives", planService.alternatives(id, itemId, user));
        return "plan-alternatives";
    }

    @PostMapping("/{id}/items/{itemId}/replace")
    public String replace(@PathVariable Long id, @PathVariable Long itemId, @RequestParam Long venueId,
                          Principal principal, RedirectAttributes redirect) {
        try {
            planService.replace(id, itemId, venueId, userService.current(principal));
            redirect.addFlashAttribute("success", "Stop replaced. The rest of your plan stayed the same.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plans/" + id;
    }

    @PostMapping("/{id}/items/{itemId}/visited")
    public String visited(@PathVariable Long id, @PathVariable Long itemId, Principal principal) {
        planService.toggleVisited(id, itemId, userService.current(principal));
        return "redirect:/plans/" + id + "#item-" + itemId;
    }

    @PostMapping("/{id}/tired")
    public String tired(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        redirect.addFlashAttribute("success", planService.kidsAreTired(id, userService.current(principal)));
        return "redirect:/plans/" + id;
    }

    @PostMapping("/{id}/save")
    public String save(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        planService.save(id, userService.current(principal));
        redirect.addFlashAttribute("success", "Plan saved. Open it once while online to keep it available offline.");
        return "redirect:/plans/" + id;
    }

    @PostMapping("/{id}/rate")
    public String rate(@PathVariable Long id, @RequestParam int rating, @RequestParam(required = false) String comment,
                       Principal principal, RedirectAttributes redirect) {
        try {
            planService.rate(id, rating, comment, userService.current(principal));
            redirect.addFlashAttribute("success", "Thank you for rating this plan.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plans/" + id;
    }

    @PostMapping("/{id}/reuse")
    public String reuse(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            Plan plan = planService.reuse(id, userService.current(principal));
            redirect.addFlashAttribute("success", "A fresh plan was created from the same details.");
            return "redirect:/plans/" + plan.getId();
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/family";
        }
    }

    @PostMapping("/{id}/share")
    public String share(@PathVariable Long id, @RequestParam String email, Principal principal,
                        RedirectAttributes redirect) {
        try {
            planService.share(id, email, userService.current(principal));
            redirect.addFlashAttribute("success", "Plan shared with " + email.trim() + ".");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plans/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        planService.delete(id, userService.current(principal));
        redirect.addFlashAttribute("success", "Plan deleted.");
        return "redirect:/plans";
    }
}
