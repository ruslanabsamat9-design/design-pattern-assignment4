package com.flightreservation.loyalty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightreservation.storage.JsonStore;
import java.util.List;

public class LoyaltyRepository {
    private final JsonStore store;
    public LoyaltyRepository(JsonStore store){this.store=store;}
    public List<LoyaltyProfile> findAll(){return store.read("loyalty.json",new TypeReference<List<LoyaltyProfile>>(){});}
    public void save(LoyaltyProfile profile){
        List<LoyaltyProfile> all=findAll(); all.removeIf(x->x.getPassenger().equalsIgnoreCase(profile.getPassenger())); all.add(profile);
        store.write("loyalty.json",all);
    }
}
