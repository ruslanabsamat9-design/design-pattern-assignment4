package com.flightreservation.ticket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightreservation.storage.JsonStore;
import java.util.List;
import java.util.Optional;

public class TicketRepository {
    private final JsonStore store;
    public TicketRepository(JsonStore store){this.store=store;}
    public List<ETicket> findAll(){return store.read("tickets.json",new TypeReference<List<ETicket>>(){});}
    public void save(ETicket item){
        List<ETicket> all=findAll(); all.removeIf(x->x.getId().equals(item.getId())); all.add(item);
        store.write("tickets.json",all);
    }
    public Optional<ETicket> find(String id){return findAll().stream().filter(x->x.getId().equals(id)).findFirst();}
}
