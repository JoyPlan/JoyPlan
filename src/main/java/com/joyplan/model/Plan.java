package com.joyplan.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 1000)
    private String summary;

    @Embedded
    private OutingRequest request = new OutingRequest();

    private int totalCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus status = PlanStatus.DRAFT;

    private Integer rating;

    @Column(length = 500)
    private String ratingComment;

    @Column(length = 60)
    private String generatedBy;

    private boolean tiredAdjusted;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<PlanItem> items = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "plan_share",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> sharedWith = new HashSet<>();

    public void replaceItems(List<PlanItem> newItems) {
        items.clear();
        int index = 0;
        for (PlanItem item : newItems) {
            item.setPlan(this);
            item.setOrderIndex(index++);
            items.add(item);
        }
        recalculateTotal();
        updatedAt = LocalDateTime.now();
    }

    public void recalculateTotal() {
        totalCost = items.stream().mapToInt(PlanItem::getCost).sum();
    }

    public List<Integer> getRunningTotals() {
        List<Integer> totals = new ArrayList<>();
        int sum = 0;
        for (PlanItem item : items) {
            sum += item.getCost();
            totals.add(sum);
        }
        return totals;
    }

    public int getBudgetUsedPercent() {
        if (request.getBudget() <= 0) {
            return totalCost == 0 ? 0 : 100;
        }
        return Math.min(100, (int) Math.round(totalCost * 100.0 / request.getBudget()));
    }

    public boolean isOwnedBy(User user) {
        return user != null && owner != null && owner.getId().equals(user.getId());
    }

    public boolean isAccessibleBy(User user) {
        if (isOwnedBy(user)) {
            return true;
        }
        return user != null && sharedWith.stream().anyMatch(u -> u.getId().equals(user.getId()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public OutingRequest getRequest() {
        return request;
    }

    public void setRequest(OutingRequest request) {
        this.request = request;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getRatingComment() {
        return ratingComment;
    }

    public void setRatingComment(String ratingComment) {
        this.ratingComment = ratingComment;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public boolean isTiredAdjusted() {
        return tiredAdjusted;
    }

    public void setTiredAdjusted(boolean tiredAdjusted) {
        this.tiredAdjusted = tiredAdjusted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PlanItem> getItems() {
        return items;
    }

    public Set<User> getSharedWith() {
        return sharedWith;
    }
}
