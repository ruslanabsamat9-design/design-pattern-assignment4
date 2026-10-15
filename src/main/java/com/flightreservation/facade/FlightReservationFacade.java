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
import java.util.UUID;

public class FlightReservationFacade {
    private final SeatReservationService seatReservationService;
    private final TicketService ticketService;
    private final LoyaltyService loyaltyService;
    private final WorkflowGateway workflowGateway;

    public FlightReservationFacade(Path dataDirectory) {
        this.seatReservationService = new SeatReservationService(dataDirectory);
        this.ticketService = new TicketService(dataDirectory);
        this.loyaltyService = new LoyaltyService(dataDirectory);
        this.workflowGateway = new WorkflowGateway(dataDirectory);
    }

    public Reservation book(String passengerName, String flightNumber, String seatNumber, double baggageKg, String mealPreference) {
        Reservation reservation = seatReservationService.reserveSeat(passengerName, flightNumber, seatNumber);
        WorkflowProgress progress = new WorkflowProgress(
                UUID.randomUUID().toString(),
                passengerName,
                reservation.getId(),
                null,
                WorkflowStatus.PENDING_B
        );
        workflowGateway.save(progress);

        ETicket ticket = ticketService.issueTicket(reservation.getId(), passengerName, baggageKg);
        progress.setTicketId(ticket.getId());
        progress.setStatus(WorkflowStatus.PENDING_C);
        workflowGateway.save(progress);

        loyaltyService.savePreference(passengerName, mealPreference);
        progress.setStatus(WorkflowStatus.COMPLETED);
        workflowGateway.save(progress);

        return reservation;
    }

    public Reservation bookWithFailure(String passengerName, String flightNumber, String seatNumber, double baggageKg,
                                      String mealPreference, String failureStage) {
        if ("B".equalsIgnoreCase(failureStage)) {
            Reservation reservation = seatReservationService.reserveSeat(passengerName, flightNumber, seatNumber);
            WorkflowProgress progress = new WorkflowProgress(
                    UUID.randomUUID().toString(),
                    passengerName,
                    reservation.getId(),
                    null,
                    WorkflowStatus.PENDING_B
            );
            workflowGateway.save(progress);
            recoverWorkflowProgress();
            return seatReservationService.findById(reservation.getId());
        }

        if ("C".equalsIgnoreCase(failureStage)) {
            Reservation reservation = seatReservationService.reserveSeat(passengerName, flightNumber, seatNumber);
            WorkflowProgress progress = new WorkflowProgress(
                    UUID.randomUUID().toString(),
                    passengerName,
                    reservation.getId(),
                    null,
                    WorkflowStatus.PENDING_B
            );
            workflowGateway.save(progress);

            ETicket ticket = ticketService.issueTicket(reservation.getId(), passengerName, baggageKg);
            progress.setTicketId(ticket.getId());
            progress.setStatus(WorkflowStatus.PENDING_C);
            workflowGateway.save(progress);

            recoverWorkflowProgress();
            return seatReservationService.findById(reservation.getId());
        }

        return book(passengerName, flightNumber, seatNumber, baggageKg, mealPreference);
    }

    public WorkflowProgress recoverWorkflowProgress() {
        WorkflowProgress progress = workflowGateway.loadLatest();
        if (progress == null) {
            return null;
        }

        WorkflowStatus status = progress.getStatus();
        if (status == WorkflowStatus.COMPLETED || status == WorkflowStatus.COMPENSATED) {
            return progress;
        }

        progress.setStatus(WorkflowStatus.COMPENSATING);
        workflowGateway.save(progress);

        if (status == WorkflowStatus.PENDING_B) {
            compensateReservation(progress.getReservationId());
        } else if (status == WorkflowStatus.PENDING_C) {
            compensateTicket(progress.getTicketId());
            compensateReservation(progress.getReservationId());
        }

        progress.setStatus(WorkflowStatus.COMPENSATED);
        workflowGateway.save(progress);
        return progress;
    }

    private void compensateTicket(String ticketId) {
        if (ticketId == null) {
            return;
        }

        ETicket ticket = ticketService.findById(ticketId);
        if (ticket.getStatus() != ETicketStatus.CANCELLED) {
            ticketService.cancelTicket(ticketId);
        }
    }

    private void compensateReservation(String reservationId) {
        if (reservationId == null) {
            return;
        }

        Reservation reservation = seatReservationService.findById(reservationId);
        if (reservation.getStatus() != ReservationStatus.CANCELLED) {
            seatReservationService.cancelReservation(reservationId);
        }
    }

    public WorkflowProgress getWorkflowProgress() {
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

    public LoyaltyProfile getProfile(String passengerName) {
        return loyaltyService.findByPassengerName(passengerName);
    }
}
