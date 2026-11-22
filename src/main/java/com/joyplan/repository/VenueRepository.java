package com.joyplan.repository;

import com.joyplan.model.Venue;
import com.joyplan.model.VenueCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByActiveTrueAndCategory(VenueCategory category);

    List<Venue> findAllByOrderByCategoryAscNameAsc();

    List<Venue> findByCategoryOrderByNameAsc(VenueCategory category);

    long countByCategory(VenueCategory category);

    long countByActiveTrue();
}
