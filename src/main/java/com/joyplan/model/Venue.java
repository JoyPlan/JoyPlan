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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VenueCategory category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Setting setting;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Area area;

    @Size(max = 200)
    @Column(length = 200)
    private String address;

    @DecimalMin("22.0")
    @DecimalMax("27.0")
    private double latitude;

    @DecimalMin("51.0")
    @DecimalMax("57.0")
    private double longitude;

    @Min(0)
    @Max(17)
    private int minAge;

    @Min(0)
    @Max(17)
    private int maxAge = 17;

    @Min(0)
    @Max(5000)
    private int priceAdult;

    @Min(0)
    @Max(5000)
    private int priceChild;

    @Min(10)
    @Max(480)
    private int durationMinutes = 60;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnergyLevel energyLevel = EnergyLevel.MEDIUM;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "venue_interest", joinColumns = @JoinColumn(name = "venue_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interest", length = 20)
    private Set<Interest> interests = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "venue_food_tag", joinColumns = @JoinColumn(name = "venue_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "food_tag", length = 20)
    private Set<FoodTag> foodTags = new HashSet<>();

    @NotNull
    @DateTimeFormat(pattern = "HH:mm")
    @Column(nullable = false)
    private LocalTime openTime = LocalTime.of(9, 0);

    @NotNull
    @DateTimeFormat(pattern = "HH:mm")
    @Column(nullable = false)
    private LocalTime closeTime = LocalTime.of(22, 0);

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    private boolean sponsored;

    private boolean active = true;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastVerified = LocalDate.now();

    public String getDirectionsUrl() {
        return String.format(Locale.ROOT,
                "https://www.google.com/maps/dir/?api=1&destination=%.6f,%.6f&travelmode=driving",
                latitude, longitude);
    }

    public String getAgeRange() {
        return minAge + "–" + maxAge + " yrs";
    }

    public boolean isStale() {
        return lastVerified == null || lastVerified.isBefore(LocalDate.now().minusDays(7));
    }

    public boolean isFree() {
        return priceAdult == 0 && priceChild == 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VenueCategory getCategory() {
        return category;
    }

    public void setCategory(VenueCategory category) {
        this.category = category;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getPriceAdult() {
        return priceAdult;
    }

    public void setPriceAdult(int priceAdult) {
        this.priceAdult = priceAdult;
    }

    public int getPriceChild() {
        return priceChild;
    }

    public void setPriceChild(int priceChild) {
        this.priceChild = priceChild;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public EnergyLevel getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(EnergyLevel energyLevel) {
        this.energyLevel = energyLevel;
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

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSponsored() {
        return sponsored;
    }

    public void setSponsored(boolean sponsored) {
        this.sponsored = sponsored;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getLastVerified() {
        return lastVerified;
    }

    public void setLastVerified(LocalDate lastVerified) {
        this.lastVerified = lastVerified;
    }
}
