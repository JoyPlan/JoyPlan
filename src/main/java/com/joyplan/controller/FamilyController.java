package com.joyplan.controller;

import com.joyplan.dto.ChildForm;
import com.joyplan.model.Area;
import com.joyplan.model.User;
import com.joyplan.service.FamilyService;
import com.joyplan.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class FamilyController {

    private final UserService userService;
    private final FamilyService familyService;

    public FamilyController(UserService userService, FamilyService familyService) {
        this.userService = userService;
        this.familyService = familyService;
    }

    @GetMapping("/family")
    public String family(Principal principal, Model model) {
        User user = userService.current(principal);
        populate(model, user);
        model.addAttribute("childForm", new ChildForm());
        return "family";
    }

    @GetMapping("/family/children/{id}/edit")
    public String edit(@PathVariable Long id, Principal principal, Model model) {
        User user = userService.current(principal);
        populate(model, user);
        model.addAttribute("childForm", ChildForm.from(familyService.child(user, id)));
        return "family";
    }

    @PostMapping("/family/children")
    public String saveChild(@Valid @ModelAttribute("childForm") ChildForm form, BindingResult result,
                            Principal principal, Model model, RedirectAttributes redirect) {
        User user = userService.current(principal);
        if (result.hasErrors()) {
            populate(model, user);
            return "family";
        }
        try {
            familyService.save(user, form);
            redirect.addFlashAttribute("success", form.getId() == null ? "Child added to your family profile." : "Child updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/family";
    }

    @PostMapping("/family/children/{id}/delete")
    public String deleteChild(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        familyService.delete(userService.current(principal), id);
        redirect.addFlashAttribute("success", "Child removed from your family profile.");
        return "redirect:/family";
    }

    @PostMapping("/family/home-area")
    public String homeArea(@RequestParam Area homeArea, Principal principal, RedirectAttributes redirect) {
        userService.updateHomeArea(userService.current(principal), homeArea);
        redirect.addFlashAttribute("success", "Home area updated.");
        return "redirect:/family";
    }

    @PostMapping("/account/delete")
    public String deleteAccount(Principal principal, HttpServletRequest request) throws ServletException {
        userService.deleteAccount(userService.current(principal));
        request.logout();
        return "redirect:/login?deleted";
    }

    private void populate(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("children", familyService.children(user));
    }
}
