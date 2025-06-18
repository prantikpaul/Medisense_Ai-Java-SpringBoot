package com.example.medisense_ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meal_plans")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private Integer totalProtein; // in grams

    @Column
    private Integer totalCarbs; // in grams

    @Column
    private Integer totalFat; // in grams

    @Column
    private Integer totalCalories; // in kcal

    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meal> meals = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "meal_plan_allergies", joinColumns = @JoinColumn(name = "meal_plan_id"))
    @Column(name = "allergy")
    private List<String> allergies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "meal_plan_dislikes", joinColumns = @JoinColumn(name = "meal_plan_id"))
    @Column(name = "dislike")
    private List<String> dislikes = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // Pre-persist hook
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        if (date == null) {
            date = LocalDate.now();
        }
    }

    // Pre-update hook
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to add a meal
    public void addMeal(Meal meal) {
        meals.add(meal);
        meal.setMealPlan(this);
    }

    // Helper method to remove a meal
    public void removeMeal(Meal meal) {
        meals.remove(meal);
        meal.setMealPlan(null);
    }
}