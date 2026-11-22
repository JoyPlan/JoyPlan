package com.joyplan.repository;

import com.joyplan.model.Plan;
import com.joyplan.model.PlanStatus;
import com.joyplan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByOwnerOrderByCreatedAtDesc(User owner);

    List<Plan> findBySharedWithContainingOrderByCreatedAtDesc(User user);

    long countByStatus(PlanStatus status);

    @Query("select avg(p.rating) from Plan p where p.rating is not null")
    Double averageRating();
}
