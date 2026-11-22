package com.joyplan.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private User parent;

    @Column(nullable = false)
    private int age;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "child_interest", joinColumns = @JoinColumn(name = "child_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interest", length = 20)
    private Set<Interest> interests = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "child_food_tag", joinColumns = @JoinColumn(name = "child_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "food_tag", length = 20)
    private Set<FoodTag> foodTags = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
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
