package com.joyplan.controller;

import com.joyplan.model.Venue;
import com.joyplan.model.VenueCategory;
import com.joyplan.service.VenueService;
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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final VenueService venueService;

    public AdminController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", venueService.stats());
        return "admin/dashboard";
    }

    @GetMapping("/venues")
    public String venues(@RequestParam(required = false) VenueCategory category, Model model) {
        model.addAttribute("venues", venueService.all(category));
        model.addAttribute("selectedCategory", category);
        return "admin/venues";
    }

    @GetMapping("/venues/new")
    public String newVenue(Model model) {
        Venue venue = new Venue();
        venue.setCategory(VenueCategory.ACTIVITY);
        venue.setLatitude(24.4764);
        venue.setLongitude(54.3213);
        model.addAttribute("venue", venue);
        return "admin/venue-form";
    }

    @GetMapping("/venues/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("venue", venueService.get(id));
        return "admin/venue-form";
    }

    @PostMapping("/venues")
    public String save(@Valid @ModelAttribute("venue") Venue venue, BindingResult result, Model model,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "admin/venue-form";
        }
        try {
            Venue saved = venueService.save(venue);
            redirect.addFlashAttribute("success", "Venue \"" + saved.getName() + "\" saved and marked as verified today.");
            return "redirect:/admin/venues";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/venue-form";
        }
    }

    @PostMapping("/venues/{id}/verify")
    public String verify(@PathVariable Long id, RedirectAttributes redirect) {
        venueService.verify(id);
        redirect.addFlashAttribute("success", "Venue marked as verified today.");
        return "redirect:/admin/venues";
    }

    @PostMapping("/venues/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        boolean active = venueService.toggleActive(id);
        redirect.addFlashAttribute("success", active ? "Venue activated." : "Venue hidden from recommendations.");
        return "redirect:/admin/venues";
    }

    @PostMapping("/venues/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        boolean deleted = venueService.delete(id);
        redirect.addFlashAttribute("success", deleted ? "Venue deleted."
                : "This venue is used in saved plans, so it was deactivated instead of deleted.");
        return "redirect:/admin/venues";
    }
}
