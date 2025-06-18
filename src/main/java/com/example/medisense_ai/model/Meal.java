package com.example.medisense_ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @Column(nullable = false)
    private String mealType; // Breakfast, Lunch, Dinner, Snack

    @Column(nullable = false)
    private String description; // e.g., "Oatmeal with banana and almonds"

    @Column
    private Integer calories; // in kcal

    @Column
    private Integer protein; // in grams

    @Column
    private Integer carbs; // in grams

    @Column
    private Integer fat; // in grams

    @Column
    private String imageUrl; // URL to meal image if available

    @Column
    private String notes; // Additional notes or instructions

    // Helper method to set meal type from enum
    public void setMealTypeEnum(MealType type) {
        this.mealType = type.name();
    }

    // Helper method to get meal type as enum
    public MealType getMealTypeEnum() {
        return MealType.valueOf(this.mealType);
    }

    // Enum for meal types
    public enum MealType {
        BREAKFAST,
        LUNCH,
        DINNER,
        SNACK
    }
}