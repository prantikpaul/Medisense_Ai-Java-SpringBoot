package com.example.medisense_ai.repository;

import com.example.medisense_ai.model.Meal;
import com.example.medisense_ai.model.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByMealPlan(MealPlan mealPlan);
    List<Meal> findByMealPlanAndMealType(MealPlan mealPlan, String mealType);
}