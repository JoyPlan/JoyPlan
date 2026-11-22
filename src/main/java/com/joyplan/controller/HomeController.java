package com.joyplan.controller;

import com.joyplan.dto.RegistrationForm;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final FamilyService familyService;
    private final PlanService planService;

    public HomeController(UserService userService, FamilyService familyService, PlanService planService) {
        this.userService = userService;
        this.familyService = familyService;
        this.planService = planService;
    }

    @GetMapping("/")
    public String index(Principal principal) {
        return principal == null ? "index" : "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login(Principal principal) {
        return principal == null ? "login" : "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.register(form);
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "duplicate", e.getMessage());
            return "register";
        }
        redirect.addFlashAttribute("success", "Account created. Please sign in.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userService.current(principal);
        List<Plan> plans = planService.plansOf(user);
        model.addAttribute("user", user);
        model.addAttribute("children", familyService.children(user));
        model.addAttribute("recentPlans", plans.stream().limit(4).toList());
        model.addAttribute("planCount", plans.size());
        model.addAttribute("sharedPlans", planService.sharedWith(user));
        return "dashboard";
    }
}
