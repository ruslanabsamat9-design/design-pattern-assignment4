package com.flightreservation.facade;

import com.flightreservation.loyalty.LoyaltyProfile;
import com.flightreservation.loyalty.LoyaltyService;
import com.flightreservation.reservation.Reservation;
import com.flightreservation.reservation.SeatReservationService;
import com.flightreservation.storage.JsonStore;
import com.flightreservation.ticket.ETicket;
import com.flightreservation.ticket.TicketService;
import com.flightreservation.workflow.*;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class FlightReservationFacade {
    private final Path directory;
    private final SeatReservationService seats;
    private final TicketService tickets;
    private final LoyaltyService loyalty;
    private final WorkflowGateway workflows;

    public FlightReservationFacade(Path directory) {
        this.directory=directory;
        JsonStore store=new JsonStore(directory);
        seats=new SeatReservationService(store); tickets=new TicketService(store);
        loyalty=new LoyaltyService(store); workflows=new WorkflowGateway(store);
    }

    public BookingReceipt book(BookingRequest request) {
        request.validate();
        Reservation reservation=seats.reserve(request.passenger(),request.flight(),request.seat());
        WorkflowProgress w=new WorkflowProgress(UUID.randomUUID().toString(),request.passenger(),
                reservation.getId(),WorkflowStatus.PENDING_B);
        workflows.save(w);
        try {
            ETicket ticket=tickets.issue(reservation.getId(),request.passenger(),request.baggageKg());
            w.setTicketId(ticket.getId()); w.setStatus(WorkflowStatus.PENDING_C); workflows.save(w);
            try {
                loyalty.savePreference(request.passenger(),request.meal());
            } catch (RuntimeException cFailure) {
                w.setFailure(cFailure.getMessage()); compensate(w); throw new IllegalStateException("Step C failed: "+cFailure.getMessage(),cFailure);
            }
            w.setStatus(WorkflowStatus.COMPLETED); workflows.save(w);
            return new BookingReceipt(w.getId(),reservation.getId(),ticket.getId());
        } catch (RuntimeException failure) {
            if(w.getStatus()==WorkflowStatus.PENDING_B) {
                w.setFailure(failure.getMessage()); compensate(w);
                throw new IllegalStateException("Step B failed; reservation cancelled: "+failure.getMessage(),failure);
            }
            throw failure;
        }
    }

    private void compensate(WorkflowProgress w) {
        w.setStatus(WorkflowStatus.COMPENSATING); workflows.save(w);
        if(w.getTicketId()!=null) tickets.voidTicket(w.getTicketId());
        seats.cancel(w.getReservationId());
        w.setStatus(WorkflowStatus.COMPENSATED); workflows.save(w);
    }

    public List<WorkflowProgress> recover() {
        List<WorkflowProgress> done=new java.util.ArrayList<>();
        for(WorkflowProgress w:workflows.unfinished()){compensate(w);done.add(w);}
        return done;
    }
    public List<WorkflowProgress> workflows(){return workflows.all();}
    public List<Reservation> reservations(){return seats.all();}
    public List<ETicket> tickets(){return tickets.all();}
    public List<LoyaltyProfile> profiles(){return loyalty.all();}
    public void reset(){for(String f:List.of("reservations.json","tickets.json","loyalty.json","workflows.json")){
        try { java.nio.file.Files.deleteIfExists(directory.resolve(f)); }
        catch(java.io.IOException e){throw new IllegalStateException(e);}
    }}
}
