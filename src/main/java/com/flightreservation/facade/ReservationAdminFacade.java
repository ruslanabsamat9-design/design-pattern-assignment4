package com.flightreservation.facade;

import com.flightreservation.loyalty.LoyaltyProfile;
import com.flightreservation.loyalty.LoyaltyService;
import com.flightreservation.reservation.Reservation;
import com.flightreservation.reservation.ReservationStatus;
import com.flightreservation.reservation.SeatReservationService;
import com.flightreservation.ticket.ETicket;
import com.flightreservation.ticket.ETicketStatus;
import com.flightreservation.ticket.TicketService;
import com.flightreservation.workflow.WorkflowGateway;
import com.flightreservation.workflow.WorkflowProgress;
import com.flightreservation.workflow.WorkflowStatus;

import java.nio.file.Path;
import java.util.List;

public class ReservationAdminFacade {
    private final SeatReservationService seatReservationService;
    private final TicketService ticketService;
    private final LoyaltyService loyaltyService;
    private final WorkflowGateway workflowGateway;

    public ReservationAdminFacade(Path dataDirectory) {
        this.seatReservationService = new SeatReservationService(dataDirectory);
        this.ticketService = new TicketService(dataDirectory);
        this.loyaltyService = new LoyaltyService(dataDirectory);
        this.workflowGateway = new WorkflowGateway(dataDirectory);
    }

    public WorkflowProgress inspectWorkflow() {
        return workflowGateway.loadLatest();
    }

    public List<Reservation> getReservations() {
        return seatReservationService.getReservations();
    }

    public List<ETicket> getTickets() {
        return ticketService.getTickets();
    }

    public List<LoyaltyProfile> getProfiles() {
        return loyaltyService.getProfiles();
    }

    public WorkflowProgress manualCompensate() {
        WorkflowProgress progress = workflowGateway.loadLatest();
        if (progress == null) {
            return null;
        }

        WorkflowStatus currentStatus = progress.getStatus();
        if (currentStatus == WorkflowStatus.COMPENSATED || currentStatus == WorkflowStatus.COMPLETED) {
            return progress;
        }

        progress.setStatus(WorkflowStatus.COMPENSATING);
        workflowGateway.save(progress);

        if (currentStatus == WorkflowStatus.PENDING_C && progress.getTicketId() != null) {
            ETicket ticket = ticketService.findById(progress.getTicketId());
            if (ticket.getStatus() != ETicketStatus.CANCELLED) {
                ticketService.cancelTicket(progress.getTicketId());
            }
        }

        if (progress.getReservationId() != null) {
            Reservation reservation = seatReservationService.findById(progress.getReservationId());
            if (reservation.getStatus() != ReservationStatus.CANCELLED) {
                seatReservationService.cancelReservation(progress.getReservationId());
            }
        }

        progress.setStatus(WorkflowStatus.COMPENSATED);
        workflowGateway.save(progress);
        return progress;
    }
}
