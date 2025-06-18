package com.example.medisense_ai.controller;

import com.example.medisense_ai.model.Meal;
import com.example.medisense_ai.model.MealPlan;
import com.example.medisense_ai.model.User;
import com.example.medisense_ai.model.UserProfile;
import com.example.medisense_ai.service.MealPlanService;
import com.example.medisense_ai.service.UserProfileService;
import com.example.medisense_ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/meal-plans")
public class MealPlanController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final MealPlanService mealPlanService;

    @Autowired
    public MealPlanController(UserService userService, UserProfileService userProfileService, MealPlanService mealPlanService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.mealPlanService = mealPlanService;
    }

    @GetMapping
    public String viewMealPlans(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<MealPlan> recentMealPlans = mealPlanService.findMealPlansByUser(user);
        
        model.addAttribute("user", user);
        model.addAttribute("mealPlans", recentMealPlans);
        return "meal-plans";
    }

    @GetMapping("/today")
    public String viewTodaysMealPlan(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        // Get today's meal plan or create a new one if it doesn't exist
        LocalDate today = LocalDate.now();
        MealPlan todaysMealPlan = mealPlanService.findMealPlanByUserAndDate(user, today)
                .orElse(new MealPlan());
        
        if (todaysMealPlan.getId() == null) {
            // This is a new meal plan
            todaysMealPlan.setUser(user);
            todaysMealPlan.setDate(today);
            todaysMealPlan.setTotalCalories(0);
            todaysMealPlan.setTotalProtein(0);
            todaysMealPlan.setTotalCarbs(0);
            todaysMealPlan.setTotalFat(0);
        }
        
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("mealPlan", todaysMealPlan);
        model.addAttribute("meals", todaysMealPlan.getId() != null ? 
                mealPlanService.findMealsByMealPlan(todaysMealPlan) : new ArrayList<>());
        return "meal-plans-today";
    }

    @GetMapping("/add")
    public String addMealPlanForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("mealPlan", new MealPlan());
        return "meal-plans-add";
    }

    @PostMapping("/add")
    public String addMealPlan(@ModelAttribute MealPlan mealPlan, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        mealPlan.setUser(user);
        
        try {
            mealPlanService.saveMealPlan(mealPlan);
            redirectAttributes.addFlashAttribute("success", "Meal plan created successfully");
            return "redirect:/meal-plans/" + mealPlan.getId() + "/meals/add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create meal plan: " + e.getMessage());
            return "redirect:/meal-plans/add";
        }
    }

    @GetMapping("/{id}")
    public String viewMealPlan(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan mealPlan = mealPlanService.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        List<Meal> meals = mealPlanService.findMealsByMealPlan(mealPlan);
        
        model.addAttribute("user", user);
        model.addAttribute("mealPlan", mealPlan);
        model.addAttribute("meals", meals);
        return "meal-plans-view";
    }

    @GetMapping("/{id}/edit")
    public String editMealPlanForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan mealPlan = mealPlanService.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("mealPlan", mealPlan);
        return "meal-plans-edit";
    }

    @PostMapping("/{id}/update")
    public String updateMealPlan(@PathVariable Long id, @ModelAttribute MealPlan mealPlan, 
                               RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan existingMealPlan = mealPlanService.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!existingMealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        // Update fields but preserve user and meals
        mealPlan.setId(existingMealPlan.getId());
        mealPlan.setUser(existingMealPlan.getUser());
        
        try {
            mealPlanService.saveMealPlan(mealPlan);
            redirectAttributes.addFlashAttribute("success", "Meal plan updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update meal plan: " + e.getMessage());
        }
        
        return "redirect:/meal-plans/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteMealPlan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan mealPlan = mealPlanService.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        try {
            mealPlanService.deleteMealPlan(id);
            redirectAttributes.addFlashAttribute("success", "Meal plan deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete meal plan: " + e.getMessage());
        }
        
        return "redirect:/meal-plans";
    }

    @GetMapping("/{mealPlanId}/meals/add")
    public String addMealForm(@PathVariable Long mealPlanId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan mealPlan = mealPlanService.findById(mealPlanId)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("mealPlan", mealPlan);
        model.addAttribute("meal", new Meal());
        return "meal-add";
    }

    @PostMapping("/{mealPlanId}/meals/add")
    public String addMeal(@PathVariable Long mealPlanId, @ModelAttribute Meal meal, 
                         RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan mealPlan = mealPlanService.findById(mealPlanId)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        meal.setMealPlan(mealPlan);
        
        try {
            mealPlanService.saveMeal(meal);
            
            // Update meal plan totals
            mealPlan.setTotalCalories(mealPlan.getTotalCalories() + meal.getCalories());
            mealPlan.setTotalProtein(mealPlan.getTotalProtein() + meal.getProtein());
            mealPlan.setTotalCarbs(mealPlan.getTotalCarbs() + meal.getCarbs());
            mealPlan.setTotalFat(mealPlan.getTotalFat() + meal.getFat());
            mealPlanService.saveMealPlan(mealPlan);
            
            redirectAttributes.addFlashAttribute("success", "Meal added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add meal: " + e.getMessage());
        }
        
        return "redirect:/meal-plans/" + mealPlanId;
    }

    @GetMapping("/{mealPlanId}/meals/{mealId}/edit")
    public String editMealForm(@PathVariable Long mealPlanId, @PathVariable Long mealId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan mealPlan = mealPlanService.findById(mealPlanId)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        Meal meal = mealPlanService.findMealById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        
        // Ensure the meal belongs to the specified meal plan
        if (!meal.getMealPlan().getId().equals(mealPlanId)) {
            throw new RuntimeException("Meal does not belong to the specified meal plan");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("mealPlan", mealPlan);
        model.addAttribute("meal", meal);
        return "meal-edit";
    }

    @PostMapping("/{mealPlanId}/meals/{mealId}/update")
    public String updateMeal(@PathVariable Long mealPlanId, @PathVariable Long mealId, 
                           @ModelAttribute Meal meal, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan mealPlan = mealPlanService.findById(mealPlanId)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        Meal existingMeal = mealPlanService.findMealById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        
        // Ensure the meal belongs to the specified meal plan
        if (!existingMeal.getMealPlan().getId().equals(mealPlanId)) {
            throw new RuntimeException("Meal does not belong to the specified meal plan");
        }
        
        // Update meal plan totals (subtract old values, add new values)
        mealPlan.setTotalCalories(mealPlan.getTotalCalories() - existingMeal.getCalories() + meal.getCalories());
        mealPlan.setTotalProtein(mealPlan.getTotalProtein() - existingMeal.getProtein() + meal.getProtein());
        mealPlan.setTotalCarbs(mealPlan.getTotalCarbs() - existingMeal.getCarbs() + meal.getCarbs());
        mealPlan.setTotalFat(mealPlan.getTotalFat() - existingMeal.getFat() + meal.getFat());
        
        // Update fields but preserve meal plan
        meal.setId(existingMeal.getId());
        meal.setMealPlan(existingMeal.getMealPlan());
        
        try {
            mealPlanService.saveMeal(meal);
            mealPlanService.saveMealPlan(mealPlan);
            redirectAttributes.addFlashAttribute("success", "Meal updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update meal: " + e.getMessage());
        }
        
        return "redirect:/meal-plans/" + mealPlanId;
    }

    @PostMapping("/{mealPlanId}/meals/{mealId}/delete")
    public String deleteMeal(@PathVariable Long mealPlanId, @PathVariable Long mealId, 
                           RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        MealPlan mealPlan = mealPlanService.findById(mealPlanId)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));
        
        // Ensure the meal plan belongs to the current user
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to meal plan");
        }
        
        Meal meal = mealPlanService.findMealById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        
        // Ensure the meal belongs to the specified meal plan
        if (!meal.getMealPlan().getId().equals(mealPlanId)) {
            throw new RuntimeException("Meal does not belong to the specified meal plan");
        }
        
        try {
            // Update meal plan totals (subtract meal values)
            mealPlan.setTotalCalories(mealPlan.getTotalCalories() - meal.getCalories());
            mealPlan.setTotalProtein(mealPlan.getTotalProtein() - meal.getProtein());
            mealPlan.setTotalCarbs(mealPlan.getTotalCarbs() - meal.getCarbs());
            mealPlan.setTotalFat(mealPlan.getTotalFat() - meal.getFat());
            mealPlanService.saveMealPlan(mealPlan);
            
            mealPlanService.deleteMeal(mealId);
            redirectAttributes.addFlashAttribute("success", "Meal deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete meal: " + e.getMessage());
        }
        
        return "redirect:/meal-plans/" + mealPlanId;
    }

    @GetMapping("/calendar")
    public String viewMealPlanCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Default to current month if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }
        
        List<MealPlan> mealPlans = mealPlanService.findMealPlansByDateRange(user, startDate, endDate);
        
        model.addAttribute("user", user);
        model.addAttribute("mealPlans", mealPlans);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "meal-plans-calendar";
    }
}