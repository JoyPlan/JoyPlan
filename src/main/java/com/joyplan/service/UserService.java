package com.joyplan.service;

import com.joyplan.dto.RegistrationForm;
import com.joyplan.model.Area;
import com.joyplan.model.Plan;
import com.joyplan.model.Role;
import com.joyplan.model.User;
import com.joyplan.repository.ChildRepository;
import com.joyplan.repository.PlanRepository;
import com.joyplan.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ChildRepository childRepository,
                       PlanRepository planRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.childRepository = childRepository;
        this.planRepository = planRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User();
        user.setEmail(email);
        user.setDisplayName(form.getDisplayName().trim());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.PARENT);
        user.setHomeArea(form.getHomeArea());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User current(Principal principal) {
        if (principal == null) {
            throw new NotFoundException("Not signed in.");
        }
        return userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    public void updateHomeArea(User user, Area area) {
        User managed = userRepository.findById(user.getId()).orElseThrow(() -> new NotFoundException("User not found."));
        managed.setHomeArea(area);
    }

    public void deleteAccount(User user) {
        User managed = userRepository.findById(user.getId()).orElseThrow(() -> new NotFoundException("User not found."));
        for (Plan shared : planRepository.findBySharedWithContainingOrderByCreatedAtDesc(managed)) {
            shared.getSharedWith().removeIf(u -> u.getId().equals(managed.getId()));
        }
        planRepository.deleteAll(planRepository.findByOwnerOrderByCreatedAtDesc(managed));
        childRepository.deleteByParent(managed);
        userRepository.delete(managed);
    }
}
