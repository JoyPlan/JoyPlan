package com.joyplan.config;

import com.joyplan.model.Area;
import com.joyplan.model.Child;
import com.joyplan.model.EnergyLevel;
import com.joyplan.model.FoodTag;
import com.joyplan.model.Interest;
import com.joyplan.model.Role;
import com.joyplan.model.Setting;
import com.joyplan.model.User;
import com.joyplan.model.Venue;
import com.joyplan.model.VenueCategory;
import com.joyplan.repository.ChildRepository;
import com.joyplan.repository.UserRepository;
import com.joyplan.repository.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.joyplan.model.EnergyLevel.HIGH;
import static com.joyplan.model.EnergyLevel.LOW;
import static com.joyplan.model.EnergyLevel.MEDIUM;
import static com.joyplan.model.Setting.INDOOR;
import static com.joyplan.model.Setting.OUTDOOR;
import static com.joyplan.model.VenueCategory.ACTIVITY;
import static com.joyplan.model.VenueCategory.BREAK;
import static com.joyplan.model.VenueCategory.RESTAURANT;
import static com.joyplan.model.VenueCategory.SNACK;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final VenueRepository venueRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ChildRepository childRepository,
                      VenueRepository venueRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.childRepository = childRepository;
        this.venueRepository = venueRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        }
        if (venueRepository.count() == 0) {
            venueRepository.saveAll(venues());
        }
    }

    private void seedUsers() {
        userRepository.save(user("admin@joyplan.ae", "Admin@123", "JoyPlan Admin", Role.ADMIN, Area.CORNICHE));
        User parent = userRepository.save(user("parent@joyplan.ae", "Parent@123", "Demo Parent", Role.PARENT, Area.AL_REEM));
        userRepository.save(user("partner@joyplan.ae", "Partner@123", "Demo Partner", Role.PARENT, Area.AL_REEM));
        childRepository.save(child(parent, 4, EnumSet.of(Interest.ANIMALS, Interest.PLAY, Interest.ART),
                EnumSet.of(FoodTag.ITALIAN, FoodTag.DESSERTS)));
        childRepository.save(child(parent, 7, EnumSet.of(Interest.SCIENCE, Interest.ART, Interest.WATER),
                EnumSet.of(FoodTag.HEALTHY)));
    }

    private User user(String email, String password, String name, Role role, Area area) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setDisplayName(name);
        u.setRole(role);
        u.setHomeArea(area);
        return u;
    }

    private Child child(User parent, int age, Set<Interest> interests, Set<FoodTag> food) {
        Child c = new Child();
        c.setParent(parent);
        c.setAge(age);
        c.setInterests(interests);
        c.setFoodTags(food);
        return c;
    }

    private List<Venue> venues() {
        List<Venue> list = new ArrayList<>();
        list.add(activity("Louvre Abu Dhabi Children's Museum", INDOOR, Area.SAADIYAT, 24.5338, 54.3982, 4, 12, 63, 0, 90, LOW, "10:00", "18:30", "Hands-on art galleries designed for young visitors.", Interest.ART, Interest.MUSIC));
        list.add(activity("Natural History Museum Abu Dhabi", INDOOR, Area.SAADIYAT, 24.5323, 54.4006, 3, 17, 63, 0, 120, LOW, "10:00", "18:30", "Dinosaurs, meteorites and the story of life on Earth.", Interest.SCIENCE, Interest.ANIMALS));
        list.add(activity("Umm Al Emarat Park", OUTDOOR, Area.MUSHRIF, 24.4497, 54.3934, 0, 17, 10, 0, 90, MEDIUM, "08:00", "23:00", "Large park with a farm, shade house and playgrounds.", Interest.NATURE, Interest.ANIMALS, Interest.PLAY));
        list.add(activity("The National Aquarium", INDOOR, Area.AL_QANA, 24.4165, 54.4985, 2, 17, 130, 105, 120, LOW, "10:00", "22:00", "Sharks, turtles and a rainforest zone at Al Qana.", Interest.ANIMALS, Interest.SCIENCE));
        list.add(activity("Yas Waterworld", OUTDOOR, Area.YAS_ISLAND, 24.4881, 54.5988, 4, 17, 295, 295, 180, HIGH, "10:00", "18:00", "Water park with slides and wave pools.", Interest.WATER, Interest.PLAY));
        list.add(activity("CLYMB Abu Dhabi", INDOOR, Area.YAS_ISLAND, 24.4889, 54.6080, 6, 17, 0, 199, 90, HIGH, "10:00", "22:00", "Indoor climbing walls and skydiving tunnel; adults can watch for free.", Interest.SPORTS));
        list.add(activity("Fun City Al Wahda", INDOOR, Area.MUSHRIF, 24.4700, 54.3720, 2, 12, 0, 80, 60, MEDIUM, "10:00", "23:00", "Indoor games and soft-play area inside the mall.", Interest.GAMES, Interest.PLAY));
        list.add(activity("Eastern Mangroves Kayak Tour", OUTDOOR, Area.MUSHRIF, 24.4560, 54.4180, 8, 17, 150, 120, 90, HIGH, "07:00", "17:00", "Guided kayak trip through the mangroves.", Interest.NATURE, Interest.WATER, Interest.SPORTS));
        list.add(activity("Corniche Family Beach", OUTDOOR, Area.CORNICHE, 24.4766, 54.3290, 0, 17, 10, 5, 90, MEDIUM, "08:00", "20:00", "Calm family beach with lifeguards and shade.", Interest.WATER, Interest.NATURE, Interest.PLAY));
        list.add(activity("Reem Central Park", OUTDOOR, Area.AL_REEM, 24.4985, 54.4054, 0, 12, 0, 0, 60, MEDIUM, "06:00", "23:00", "Free park with playgrounds and splash areas.", Interest.PLAY, Interest.NATURE));
        list.add(activity("Warner Bros. World Abu Dhabi", INDOOR, Area.YAS_ISLAND, 24.4892, 54.5935, 3, 17, 345, 345, 240, HIGH, "11:00", "20:00", "Indoor theme park with rides and live shows.", Interest.PLAY, Interest.GAMES));
        list.add(activity("Cultural Foundation Children's Library", INDOOR, Area.CORNICHE, 24.4870, 54.3560, 0, 12, 0, 0, 60, LOW, "09:00", "20:00", "Quiet children's library with story corners.", Interest.READING, Interest.ART));
        list.add(activity("Saadiyat Public Beach", OUTDOOR, Area.SAADIYAT, 24.5450, 54.4330, 3, 17, 25, 15, 120, MEDIUM, "08:00", "20:00", "Soft-sand beach with clear, shallow water.", Interest.WATER, Interest.NATURE));
        list.add(activity("Khalifa Park", OUTDOOR, Area.AL_QANA, 24.4300, 54.4560, 0, 17, 5, 0, 90, MEDIUM, "15:00", "22:00", "Gardens, fountains and a small train ride.", Interest.NATURE, Interest.PLAY));
        list.add(activity("Al Raha Pottery Painting Studio", INDOOR, Area.AL_RAHA, 24.4500, 54.5960, 4, 14, 0, 75, 60, LOW, "10:00", "22:00", "Paint your own mug or plate to take home.", Interest.ART));
        list.add(activity("Yas Bay Waterfront Walk", OUTDOOR, Area.YAS_ISLAND, 24.4690, 54.6070, 0, 17, 0, 0, 45, LOW, "08:00", "23:30", "Easy waterfront promenade with sea views.", Interest.NATURE));
        list.add(activity("Khalifa City Bowling Lanes", INDOOR, Area.KHALIFA_CITY, 24.4200, 54.5750, 5, 17, 40, 40, 60, MEDIUM, "10:00", "23:00", "Family bowling with bumper lanes for kids.", Interest.SPORTS, Interest.GAMES));
        Venue trampoline = activity("Jump Trampoline Park Khalifa City", INDOOR, Area.KHALIFA_CITY, 24.4230, 54.5790, 5, 17, 0, 90, 60, HIGH, "10:00", "22:00", "Trampolines, foam pits and ninja course.", Interest.SPORTS, Interest.PLAY);
        trampoline.setSponsored(true);
        list.add(trampoline);
        list.add(activity("Reem Mall Science Discovery Zone", INDOOR, Area.AL_REEM, 24.4920, 54.4110, 4, 14, 0, 50, 75, MEDIUM, "10:00", "22:00", "Interactive science experiments and building games.", Interest.SCIENCE, Interest.GAMES));

        list.add(food(RESTAURANT, "The Cheesecake Factory Yas Mall", INDOOR, Area.YAS_ISLAND, 24.4889, 54.6080, 90, 50, 60, "10:00", "23:00", FoodTag.ITALIAN, FoodTag.BURGERS, FoodTag.DESSERTS));
        list.add(food(RESTAURANT, "Al Fanar Restaurant Al Qana", INDOOR, Area.AL_QANA, 24.4160, 54.4990, 70, 40, 60, "11:00", "23:00", FoodTag.ARABIC));
        list.add(food(RESTAURANT, "Wagamama The Galleria", INDOOR, Area.AL_MARYAH, 24.4990, 54.3880, 70, 40, 60, "10:00", "23:00", FoodTag.ASIAN, FoodTag.HEALTHY, FoodTag.VEGETARIAN));
        list.add(food(RESTAURANT, "PizzaExpress Reem Mall", INDOOR, Area.AL_REEM, 24.4920, 54.4110, 60, 35, 60, "10:00", "23:00", FoodTag.ITALIAN, FoodTag.VEGETARIAN));
        list.add(food(RESTAURANT, "Bateel Cafe Saadiyat", INDOOR, Area.SAADIYAT, 24.5330, 54.4310, 80, 45, 60, "08:00", "23:00", FoodTag.ARABIC, FoodTag.HEALTHY, FoodTag.DESSERTS));
        list.add(food(RESTAURANT, "Burger Fuel Khalifa City", INDOOR, Area.KHALIFA_CITY, 24.4210, 54.5790, 50, 30, 45, "11:00", "23:30", FoodTag.BURGERS));
        list.add(food(RESTAURANT, "Lebanese Flower Al Khalidiyah", INDOOR, Area.CORNICHE, 24.4780, 54.3400, 45, 25, 60, "08:00", "23:30", FoodTag.ARABIC, FoodTag.VEGETARIAN));
        list.add(food(RESTAURANT, "SALT Corniche Beach", OUTDOOR, Area.CORNICHE, 24.4740, 54.3260, 55, 35, 45, "12:00", "23:00", FoodTag.BURGERS));
        list.add(food(RESTAURANT, "Al Raha Beach Seafood Grill", OUTDOOR, Area.AL_RAHA, 24.4520, 54.5980, 85, 45, 60, "12:00", "23:00", FoodTag.ARABIC, FoodTag.HEALTHY));

        list.add(food(SNACK, "Baskin Robbins Al Wahda Mall", INDOOR, Area.MUSHRIF, 24.4700, 54.3720, 15, 15, 20, "10:00", "23:00", FoodTag.DESSERTS));
        list.add(food(SNACK, "Reem Park Smoothie Kiosk", OUTDOOR, Area.AL_REEM, 24.4985, 54.4050, 20, 15, 20, "08:00", "22:00", FoodTag.HEALTHY, FoodTag.VEGETARIAN));
        list.add(food(SNACK, "Tim Hortons Yas Mall", INDOOR, Area.YAS_ISLAND, 24.4889, 54.6080, 20, 15, 20, "08:00", "23:30", FoodTag.DESSERTS));
        list.add(food(SNACK, "Saadiyat Beach Kiosk", OUTDOOR, Area.SAADIYAT, 24.5440, 54.4320, 20, 15, 20, "09:00", "19:00", FoodTag.HEALTHY));
        list.add(food(SNACK, "Corniche Juice and Popcorn Stand", OUTDOOR, Area.CORNICHE, 24.4760, 54.3300, 15, 10, 20, "09:00", "22:00", FoodTag.HEALTHY, FoodTag.VEGETARIAN));
        list.add(food(SNACK, "Joe and The Juice Al Qana", INDOOR, Area.AL_QANA, 24.4162, 54.4988, 30, 20, 20, "08:00", "23:00", FoodTag.HEALTHY, FoodTag.VEGETARIAN));
        list.add(food(SNACK, "Magnolia Bakery The Galleria", INDOOR, Area.AL_MARYAH, 24.4995, 54.3885, 25, 20, 25, "09:00", "23:00", FoodTag.DESSERTS));
        list.add(food(SNACK, "Khalifa City Date and Coffee Corner", INDOOR, Area.KHALIFA_CITY, 24.4195, 54.5765, 15, 10, 20, "07:00", "23:00", FoodTag.ARABIC));
        list.add(food(SNACK, "Saadiyat Cultural District Cafe", INDOOR, Area.SAADIYAT, 24.5335, 54.3995, 25, 15, 20, "09:00", "20:00", FoodTag.HEALTHY, FoodTag.DESSERTS));

        list.add(rest("Yas Mall Family Lounge", INDOOR, Area.YAS_ISLAND, 24.4887, 54.6075, "10:00", "23:00"));
        list.add(rest("Umm Al Emarat Shaded Picnic Lawn", OUTDOOR, Area.MUSHRIF, 24.4502, 54.3940, "08:00", "23:00"));
        list.add(rest("Corniche Shaded Benches", OUTDOOR, Area.CORNICHE, 24.4770, 54.3310, "06:00", "23:30"));
        list.add(rest("Reem Mall Family Rest Area", INDOOR, Area.AL_REEM, 24.4922, 54.4112, "10:00", "23:00"));
        list.add(rest("Saadiyat Cultural District Garden", OUTDOOR, Area.SAADIYAT, 24.5330, 54.4000, "08:00", "22:00"));
        list.add(rest("Al Qana Waterfront Seating", OUTDOOR, Area.AL_QANA, 24.4168, 54.4980, "08:00", "23:30"));
        list.add(rest("The Galleria Quiet Room", INDOOR, Area.AL_MARYAH, 24.4992, 54.3882, "10:00", "23:00"));
        list.add(rest("Khalifa City Community Park", OUTDOOR, Area.KHALIFA_CITY, 24.4185, 54.5780, "06:00", "23:00"));
        list.add(rest("Al Raha Beach Promenade Shade", OUTDOOR, Area.AL_RAHA, 24.4510, 54.5970, "06:00", "23:30"));
        list.add(rest("Al Wahda Mall Family Room", INDOOR, Area.MUSHRIF, 24.4703, 54.3718, "10:00", "23:00"));

        list.get(4).setLastVerified(LocalDate.now().minusDays(12));
        list.get(10).setLastVerified(LocalDate.now().minusDays(9));
        return list;
    }

    private Venue base(String name, VenueCategory category, Setting setting, Area area, double lat, double lng,
                       String open, String close) {
        Venue v = new Venue();
        v.setName(name);
        v.setCategory(category);
        v.setSetting(setting);
        v.setArea(area);
        v.setAddress(area.getLabel() + ", Abu Dhabi");
        v.setLatitude(lat);
        v.setLongitude(lng);
        v.setOpenTime(LocalTime.parse(open));
        v.setCloseTime(LocalTime.parse(close));
        v.setLastVerified(LocalDate.now());
        return v;
    }

    private Venue activity(String name, Setting setting, Area area, double lat, double lng, int minAge, int maxAge,
                           int priceAdult, int priceChild, int minutes, EnergyLevel energy, String open, String close,
                           String description, Interest... interests) {
        Venue v = base(name, ACTIVITY, setting, area, lat, lng, open, close);
        v.setMinAge(minAge);
        v.setMaxAge(maxAge);
        v.setPriceAdult(priceAdult);
        v.setPriceChild(priceChild);
        v.setDurationMinutes(minutes);
        v.setEnergyLevel(energy);
        v.setDescription(description);
        v.setInterests(EnumSet.copyOf(List.of(interests)));
        return v;
    }

    private Venue food(VenueCategory category, String name, Setting setting, Area area, double lat, double lng,
                       int priceAdult, int priceChild, int minutes, String open, String close, FoodTag... tags) {
        Venue v = base(name, category, setting, area, lat, lng, open, close);
        v.setMinAge(0);
        v.setMaxAge(17);
        v.setPriceAdult(priceAdult);
        v.setPriceChild(priceChild);
        v.setDurationMinutes(minutes);
        v.setEnergyLevel(LOW);
        v.setDescription(category == RESTAURANT ? "Family-friendly restaurant with a kids' menu." : "Quick snack stop.");
        v.setFoodTags(EnumSet.copyOf(List.of(tags)));
        return v;
    }

    private Venue rest(String name, Setting setting, Area area, double lat, double lng, String open, String close) {
        Venue v = base(name, BREAK, setting, area, lat, lng, open, close);
        v.setMinAge(0);
        v.setMaxAge(17);
        v.setDurationMinutes(20);
        v.setEnergyLevel(LOW);
        v.setDescription("Free, quiet place to sit, drink water and rest.");
        return v;
    }
}
