package com.flightreservation.loyalty;

import com.flightreservation.storage.JsonStore;
import java.util.List;

public class LoyaltyService {
    private final LoyaltyRepository repository;
    private final MealCatalog catalog=new MealCatalog();
    public LoyaltyService(JsonStore store){repository=new LoyaltyRepository(store);}
    public void savePreference(String passenger,String meal){
        String validMeal=catalog.validate(meal);
        List<LoyaltyProfile> all=repository.findAll();
        LoyaltyProfile profile=all.stream().filter(p->p.getPassenger().equalsIgnoreCase(passenger)).findFirst()
                .orElse(new LoyaltyProfile(passenger,validMeal,0));
        // Keep the profile simple and award 100 points for each successful booking.
        profile = new LoyaltyProfile(passenger,validMeal,profile.getPoints()+100);
        repository.save(profile);
    }
    public List<LoyaltyProfile> all(){return repository.findAll();}
}
