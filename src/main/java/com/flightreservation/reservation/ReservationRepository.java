package com.flightreservation.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightreservation.storage.JsonStore;
import java.util.List;
import java.util.Optional;

public class ReservationRepository {
    private final JsonStore store;
    public ReservationRepository(JsonStore store){this.store=store;}
    public List<Reservation> findAll(){return store.read("reservations.json", new TypeReference<List<Reservation>>(){});}
    public void save(Reservation item){
        List<Reservation> all=findAll(); all.removeIf(x -> x.getId().equals(item.getId())); all.add(item);
        store.write("reservations.json", all);
    }
    public Optional<Reservation> find(String id){return findAll().stream().filter(x->x.getId().equals(id)).findFirst();}
}
