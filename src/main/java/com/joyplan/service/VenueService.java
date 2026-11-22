package com.joyplan.service;

import com.joyplan.model.Venue;
import com.joyplan.model.VenueCategory;
import com.joyplan.repository.PlanItemRepository;
import com.joyplan.repository.PlanRepository;
import com.joyplan.repository.UserRepository;
import com.joyplan.repository.VenueRepository;
import com.joyplan.model.PlanStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;
    private final PlanItemRepository planItemRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public VenueService(VenueRepository venueRepository, PlanItemRepository planItemRepository,
                        PlanRepository planRepository, UserRepository userRepository) {
        this.venueRepository = venueRepository;
        this.planItemRepository = planItemRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Venue> all(VenueCategory category) {
        return category == null ? venueRepository.findAllByOrderByCategoryAscNameAsc()
                : venueRepository.findByCategoryOrderByNameAsc(category);
    }

    @Transactional(readOnly = true)
    public Venue get(Long id) {
        return venueRepository.findById(id).orElseThrow(() -> new NotFoundException("Venue not found."));
    }

    public Venue save(Venue form) {
        if (form.getMinAge() > form.getMaxAge()) {
            throw new IllegalArgumentException("Minimum age cannot be greater than maximum age.");
        }
        if (!form.getOpenTime().isBefore(form.getCloseTime())) {
            throw new IllegalArgumentException("Opening time must be before closing time.");
        }
        Venue venue = form.getId() == null ? new Venue() : get(form.getId());
        venue.setName(form.getName().trim());
        venue.setCategory(form.getCategory());
        venue.setSetting(form.getSetting());
        venue.setArea(form.getArea());
        venue.setAddress(form.getAddress());
        venue.setLatitude(form.getLatitude());
        venue.setLongitude(form.getLongitude());
        venue.setMinAge(form.getMinAge());
        venue.setMaxAge(form.getMaxAge());
        venue.setPriceAdult(form.getPriceAdult());
        venue.setPriceChild(form.getPriceChild());
        venue.setDurationMinutes(form.getDurationMinutes());
        venue.setEnergyLevel(form.getEnergyLevel());
        venue.setInterests(new HashSet<>(form.getInterests()));
        venue.setFoodTags(new HashSet<>(form.getFoodTags()));
        venue.setOpenTime(form.getOpenTime());
        venue.setCloseTime(form.getCloseTime());
        venue.setDescription(form.getDescription());
        venue.setSponsored(form.isSponsored());
        venue.setActive(form.isActive());
        venue.setLastVerified(LocalDate.now());
        return venueRepository.save(venue);
    }

    public void verify(Long id) {
        get(id).setLastVerified(LocalDate.now());
    }

    public boolean toggleActive(Long id) {
        Venue venue = get(id);
        venue.setActive(!venue.isActive());
        return venue.isActive();
    }

    public boolean delete(Long id) {
        Venue venue = get(id);
        if (planItemRepository.existsByVenueId(id)) {
            venue.setActive(false);
            return false;
        }
        venueRepository.delete(venue);
        return true;
    }

    @Transactional(readOnly = true)
    public AdminStats stats() {
        List<Venue> venues = venueRepository.findAll();
        Map<VenueCategory, Long> byCategory = new EnumMap<>(VenueCategory.class);
        for (VenueCategory category : VenueCategory.values()) {
            byCategory.put(category, venueRepository.countByCategory(category));
        }
        long stale = venues.stream().filter(Venue::isStale).count();
        List<Object[]> top = planItemRepository.countByVenue().stream().limit(5).toList();
        return new AdminStats(userRepository.count(), venues.size(), venueRepository.countByActiveTrue(), stale,
                planRepository.count(), planRepository.countByStatus(PlanStatus.SAVED),
                planRepository.countByStatus(PlanStatus.COMPLETED), planRepository.averageRating(), byCategory, top);
    }
}
