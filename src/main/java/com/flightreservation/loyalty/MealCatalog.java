package com.flightreservation.loyalty;

import java.util.Set;
public class MealCatalog {
    private final Set<String> meals=Set.of("Standard","Vegetarian","Vegan","Kosher","Halal","Gluten-Free");
    public String validate(String meal){
        if(meal==null || meals.stream().noneMatch(x->x.equalsIgnoreCase(meal.trim())))
            throw new IllegalArgumentException("Meal not available. Choose Standard, Vegetarian, Vegan, Kosher, Halal or Gluten-Free");
        return meals.stream().filter(x->x.equalsIgnoreCase(meal.trim())).findFirst().orElseThrow();
    }
}
