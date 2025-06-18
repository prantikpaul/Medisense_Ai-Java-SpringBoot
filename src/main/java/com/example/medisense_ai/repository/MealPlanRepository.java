package com.example.medisense_ai.repository;

import com.example.medisense_ai.model.MealPlan;
import com.example.medisense_ai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    List<MealPlan> findByUser(User user);
    List<MealPlan> findByUserOrderByDateDesc(User user);
    Optional<MealPlan> findByUserAndDate(User user, LocalDate date);
    List<MealPlan> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
}