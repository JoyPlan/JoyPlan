package com.joyplan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalTime;

@Entity
@Table(name = "plan_item")
public class PlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private int orderIndex;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private int travelMinutes;

    private int cost;

    @Column(length = 600)
    private String reason;

    private boolean visited;

    public PlanItem() {
    }

    public PlanItem(Venue venue, LocalTime startTime, LocalTime endTime, int travelMinutes, int cost, String reason) {
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.travelMinutes = travelMinutes;
        this.cost = cost;
        this.reason = reason;
    }

    public PlanItem copy() {
        PlanItem c = new PlanItem(venue, startTime, endTime, travelMinutes, cost, reason);
        c.visited = visited;
        return c;
    }

    public long getDurationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getTravelMinutes() {
        return travelMinutes;
    }

    public void setTravelMinutes(int travelMinutes) {
        this.travelMinutes = travelMinutes;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
