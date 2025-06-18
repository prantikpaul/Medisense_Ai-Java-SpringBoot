package com.example.medisense_ai.service;

import com.example.medisense_ai.model.Meal;
import com.example.medisense_ai.model.MealPlan;
import com.example.medisense_ai.model.User;
import com.example.medisense_ai.repository.MealPlanRepository;
import com.example.medisense_ai.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealRepository mealRepository;

    @Autowired
    public MealPlanService(MealPlanRepository mealPlanRepository, MealRepository mealRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.mealRepository = mealRepository;
    }

    @Transactional
    public MealPlan saveMealPlan(MealPlan mealPlan) {
        return mealPlanRepository.save(mealPlan);
    }

    @Transactional
    public MealPlan saveMealPlanWithMeals(MealPlan mealPlan, List<Meal> meals) {
        MealPlan savedMealPlan = mealPlanRepository.save(mealPlan);
        
        if (meals != null && !meals.isEmpty()) {
            for (Meal meal : meals) {
                meal.setMealPlan(savedMealPlan);
                mealRepository.save(meal);
            }
        }
        
        return savedMealPlan;
    }

    public Optional<MealPlan> findById(Long id) {
        return mealPlanRepository.findById(id);
    }

    public List<MealPlan> findAllByUser(User user) {
        return mealPlanRepository.findByUser(user);
    }

    public List<MealPlan> findRecentMealPlansByUser(User user) {
        return mealPlanRepository.findByUserOrderByDateDesc(user);
    }

    public Optional<MealPlan> findMealPlanByDate(User user, LocalDate date) {
        return mealPlanRepository.findByUserAndDate(user, date);
    }

    public List<MealPlan> findMealPlansByDateRange(User user, LocalDate start, LocalDate end) {
        return mealPlanRepository.findByUserAndDateBetween(user, start, end);
    }

    public List<Meal> findMealsByMealPlan(MealPlan mealPlan) {
        return mealRepository.findByMealPlan(mealPlan);
    }

    public List<Meal> findMealsByMealPlanAndType(MealPlan mealPlan, String mealType) {
        return mealRepository.findByMealPlanAndMealType(mealPlan, mealType);
    }

    @Transactional
    public void deleteMealPlan(Long id) {
        mealPlanRepository.deleteById(id);
    }

    @Transactional
    public Meal saveMeal(Meal meal) {
        return mealRepository.save(meal);
    }

    public Optional<Meal> findMealById(Long id) {
        return mealRepository.findById(id);
    }

    @Transactional
    public void deleteMeal(Long id) {
        mealRepository.deleteById(id);
    }
}