package com.joyplan.repository;

import com.joyplan.model.PlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {

    boolean existsByVenueId(Long venueId);

    @Query("select i.venue.name, count(i) from PlanItem i group by i.venue.name order by count(i) desc")
    List<Object[]> countByVenue();
}
