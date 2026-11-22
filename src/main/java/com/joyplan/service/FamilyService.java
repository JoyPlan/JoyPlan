package com.joyplan.service;

import com.joyplan.dto.ChildForm;
import com.joyplan.model.Child;
import com.joyplan.model.User;
import com.joyplan.repository.ChildRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class FamilyService {

    private static final int MAX_CHILDREN = 8;

    private final ChildRepository childRepository;

    public FamilyService(ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    @Transactional(readOnly = true)
    public List<Child> children(User parent) {
        return childRepository.findByParentOrderByAgeAsc(parent);
    }

    @Transactional(readOnly = true)
    public Child child(User parent, Long id) {
        return childRepository.findByIdAndParent(id, parent)
                .orElseThrow(() -> new NotFoundException("Child not found."));
    }

    public Child save(User parent, ChildForm form) {
        Child child;
        if (form.getId() == null) {
            if (childRepository.findByParentOrderByAgeAsc(parent).size() >= MAX_CHILDREN) {
                throw new IllegalArgumentException("A family profile can have at most " + MAX_CHILDREN + " children.");
            }
            child = new Child();
            child.setParent(parent);
        } else {
            child = child(parent, form.getId());
        }
        child.setAge(form.getAge());
        child.setInterests(new HashSet<>(form.getInterests()));
        child.setFoodTags(new HashSet<>(form.getFoodTags()));
        return childRepository.save(child);
    }

    public void delete(User parent, Long id) {
        childRepository.delete(child(parent, id));
    }
}
