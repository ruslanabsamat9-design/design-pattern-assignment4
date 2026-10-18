package com.flightreservation.ticket;

import com.flightreservation.storage.JsonStore;
import java.util.List;
import java.util.UUID;

public class TicketService {
    private final TicketRepository repository;
    private final BaggageRules rules=new BaggageRules();
    public TicketService(JsonStore store){repository=new TicketRepository(store);}
    public ETicket issue(String reservationId,String passenger,double baggageKg){
        rules.validate(baggageKg);
        ETicket t=new ETicket(UUID.randomUUID().toString(),reservationId,passenger,baggageKg);
        repository.save(t); return t;
    }
    public void voidTicket(String id){
        ETicket t=repository.find(id).orElseThrow(()->new IllegalArgumentException("Ticket not found"));
        if(t.getStatus()!=TicketStatus.VOIDED){t.voidTicket();repository.save(t);}
    }
    public List<ETicket> all(){return repository.findAll();}
}
