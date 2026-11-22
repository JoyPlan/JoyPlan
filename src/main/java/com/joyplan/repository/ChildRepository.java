package com.joyplan.repository;

import com.joyplan.model.Child;
import com.joyplan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChildRepository extends JpaRepository<Child, Long> {

    List<Child> findByParentOrderByAgeAsc(User parent);

    Optional<Child> findByIdAndParent(Long id, User parent);

    void deleteByParent(User parent);
}
