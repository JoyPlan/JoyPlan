package com.joyplan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
public class OutingRequest {

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Area area;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Mood mood;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EnergyLevel energy;

    private int budget;

    private int maxDistanceKm;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate outingDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SettingPreference setting;

    private int adults;

    private boolean includeMeal;

    public OutingRequest copy() {
        OutingRequest c = new OutingRequest();
        c.area = area;
        c.mood = mood;
        c.energy = energy;
        c.budget = budget;
        c.maxDistanceKm = maxDistanceKm;
        c.startTime = startTime;
        c.endTime = endTime;
        c.outingDate = outingDate;
        c.setting = setting;
        c.adults = adults;
        c.includeMeal = includeMeal;
        return c;
    }

    public long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public EnergyLevel getEnergy() {
        return energy;
    }

    public void setEnergy(EnergyLevel energy) {
        this.energy = energy;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public int getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(int maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
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

    public LocalDate getOutingDate() {
        return outingDate;
    }

    public void setOutingDate(LocalDate outingDate) {
        this.outingDate = outingDate;
    }

    public SettingPreference getSetting() {
        return setting;
    }

    public void setSetting(SettingPreference setting) {
        this.setting = setting;
    }

    public int getAdults() {
        return adults;
    }

    public void setAdults(int adults) {
        this.adults = adults;
    }

    public boolean isIncludeMeal() {
        return includeMeal;
    }

    public void setIncludeMeal(boolean includeMeal) {
        this.includeMeal = includeMeal;
    }
}
