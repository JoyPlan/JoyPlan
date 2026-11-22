package com.joyplan.dto;

import com.joyplan.model.Area;
import com.joyplan.model.EnergyLevel;
import com.joyplan.model.Mood;
import com.joyplan.model.OutingRequest;
import com.joyplan.model.SettingPreference;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class OutingForm {

    @NotNull(message = "Please choose a starting area.")
    private Area area;

    @NotNull
    private Mood mood = Mood.HAPPY;

    @NotNull
    private EnergyLevel energy = EnergyLevel.MEDIUM;

    @NotNull(message = "Please enter a budget.")
    @Min(value = 0, message = "Budget cannot be negative.")
    @Max(value = 10000, message = "Budget must be at most AED 10,000.")
    private Integer budget = 300;

    @NotNull(message = "Please enter a maximum distance.")
    @Min(value = 1, message = "Distance must be at least 1 km.")
    @Max(value = 60, message = "Distance must be at most 60 km.")
    private Integer maxDistanceKm = 15;

    @NotNull(message = "Please enter a start time.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime = LocalTime.of(10, 0);

    @NotNull(message = "Please enter an end time.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime = LocalTime.of(14, 0);

    @NotNull(message = "Please choose a date.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate outingDate = LocalDate.now();

    @NotNull
    private SettingPreference setting = SettingPreference.ANY;

    @NotNull
    @Min(value = 1, message = "At least one adult is required.")
    @Max(value = 6, message = "At most 6 adults.")
    private Integer adults = 2;

    private boolean includeMeal = true;

    @AssertTrue(message = "The end time must be at least 60 minutes after the start time.")
    public boolean isValidWindow() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return Duration.between(startTime, endTime).toMinutes() >= 60;
    }

    public OutingRequest toRequest() {
        OutingRequest r = new OutingRequest();
        r.setArea(area);
        r.setMood(mood);
        r.setEnergy(energy);
        r.setBudget(budget);
        r.setMaxDistanceKm(maxDistanceKm);
        r.setStartTime(startTime);
        r.setEndTime(endTime);
        r.setOutingDate(outingDate);
        r.setSetting(setting);
        r.setAdults(adults);
        r.setIncludeMeal(includeMeal);
        return r;
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

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }

    public Integer getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(Integer maxDistanceKm) {
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

    public Integer getAdults() {
        return adults;
    }

    public void setAdults(Integer adults) {
        this.adults = adults;
    }

    public boolean isIncludeMeal() {
        return includeMeal;
    }

    public void setIncludeMeal(boolean includeMeal) {
        this.includeMeal = includeMeal;
    }
}
