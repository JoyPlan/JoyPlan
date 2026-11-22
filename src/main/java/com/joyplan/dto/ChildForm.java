package com.joyplan.dto;

import com.joyplan.model.Child;
import com.joyplan.model.FoodTag;
import com.joyplan.model.Interest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ChildForm {

    private Long id;

    @NotNull(message = "Please enter the child's age.")
    @Min(value = 0, message = "Age must be between 0 and 17.")
    @Max(value = 17, message = "Age must be between 0 and 17.")
    private Integer age;

    private Set<Interest> interests = new HashSet<>();

    private Set<FoodTag> foodTags = new HashSet<>();

    public static ChildForm from(Child child) {
        ChildForm form = new ChildForm();
        form.id = child.getId();
        form.age = child.getAge();
        form.interests = new HashSet<>(child.getInterests());
        form.foodTags = new HashSet<>(child.getFoodTags());
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Set<Interest> getInterests() {
        return interests;
    }

    public void setInterests(Set<Interest> interests) {
        this.interests = interests;
    }

    public Set<FoodTag> getFoodTags() {
        return foodTags;
    }

    public void setFoodTags(Set<FoodTag> foodTags) {
        this.foodTags = foodTags;
    }
}
