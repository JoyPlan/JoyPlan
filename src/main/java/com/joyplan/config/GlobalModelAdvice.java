package com.joyplan.config;

import com.joyplan.model.Area;
import com.joyplan.model.EnergyLevel;
import com.joyplan.model.FoodTag;
import com.joyplan.model.Interest;
import com.joyplan.model.Mood;
import com.joyplan.model.Setting;
import com.joyplan.model.SettingPreference;
import com.joyplan.model.VenueCategory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("areas")
    public Area[] areas() {
        return Area.values();
    }

    @ModelAttribute("moods")
    public Mood[] moods() {
        return Mood.values();
    }

    @ModelAttribute("energyLevels")
    public EnergyLevel[] energyLevels() {
        return EnergyLevel.values();
    }

    @ModelAttribute("interestOptions")
    public Interest[] interestOptions() {
        return Interest.values();
    }

    @ModelAttribute("foodOptions")
    public FoodTag[] foodOptions() {
        return FoodTag.values();
    }

    @ModelAttribute("settingPreferences")
    public SettingPreference[] settingPreferences() {
        return SettingPreference.values();
    }

    @ModelAttribute("settings")
    public Setting[] settings() {
        return Setting.values();
    }

    @ModelAttribute("categories")
    public VenueCategory[] categories() {
        return VenueCategory.values();
    }
}
