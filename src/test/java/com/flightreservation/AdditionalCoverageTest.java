package com.flightreservation;

import com.flightreservation.facade.FlightReservationFacade;
import com.flightreservation.facade.ReservationAdminFacade;
import com.flightreservation.loyalty.LoyaltyProfile;
import com.flightreservation.loyalty.LoyaltyStatus;
import com.flightreservation.reservation.Reservation;
import com.flightreservation.reservation.ReservationStatus;
import com.flightreservation.ticket.ETicket;
import com.flightreservation.ticket.ETicketStatus;
import com.flightreservation.workflow.WorkflowGateway;
import com.flightreservation.workflow.WorkflowProgress;
import com.flightreservation.workflow.WorkflowStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AdditionalCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void reservationTicketAndWorkflowObjectsCoverStateTransitions() {
        Reservation reservation = new Reservation();
        reservation.setId("res-1");
        reservation.setPassengerName("Sarah");
        reservation.setFlightNumber("FZ-700");
        reservation.setSeatNumber("15C");
        reservation.setStatus(ReservationStatus.PENDING);

        reservation.confirm();
        reservation.cancel();
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());

        IllegalStateException cancelledAlready = assertThrows(IllegalStateException.class, reservation::cancel);
        assertTrue(cancelledAlready.getMessage().contains("already cancelled"));

        Reservation cancelledReservation = new Reservation("res-2", "Sam", "FZ-701", "19D", ReservationStatus.CANCELLED);
        IllegalStateException cannotConfirmCancelled = assertThrows(IllegalStateException.class, cancelledReservation::confirm);
        assertTrue(cannotConfirmCancelled.getMessage().contains("Cancelled reservation"));

        ETicket ticket = new ETicket();
        ticket.setId("ticket-1");
        ticket.setReservationId("res-1");
        ticket.setPassengerName("Sarah");
        ticket.setBaggageKg(10.5);
        ticket.setStatus(ETicketStatus.PENDING);

        ticket.issue();
        assertEquals(ETicketStatus.ISSUED, ticket.getStatus());
        ticket.cancel();
        assertEquals(ETicketStatus.CANCELLED, ticket.getStatus());

        IllegalStateException ticketAlreadyCancelled = assertThrows(IllegalStateException.class, ticket::cancel);
        assertTrue(ticketAlreadyCancelled.getMessage().contains("already cancelled"));

        ETicket cancelledTicket = new ETicket("ticket-2", "res-2", "Sam", 2.0, ETicketStatus.CANCELLED);
        IllegalStateException cannotIssueCancelled = assertThrows(IllegalStateException.class, cancelledTicket::issue);
        assertTrue(cannotIssueCancelled.getMessage().contains("Cancelled ticket"));

        WorkflowProgress workflowProgress = new WorkflowProgress();
        workflowProgress.setWorkflowId("wf-1");
        workflowProgress.setPassengerName("Sarah");
        workflowProgress.setReservationId("res-1");
        workflowProgress.setTicketId("ticket-1");
        workflowProgress.setStatus(WorkflowStatus.PENDING_C);
        assertEquals("wf-1", workflowProgress.getWorkflowId());
        assertEquals("Sarah", workflowProgress.getPassengerName());
        assertEquals("res-1", workflowProgress.getReservationId());
        assertEquals("ticket-1", workflowProgress.getTicketId());
        assertEquals(WorkflowStatus.PENDING_C, workflowProgress.getStatus());

        LoyaltyProfile profile = new LoyaltyProfile();
        profile.setPassengerName("Sarah");
        profile.setMealPreference("Vegetarian");
        profile.setPoints(5);
        profile.setStatus(LoyaltyStatus.ACTIVE);

        profile.savePreference("Vegan");
        assertEquals("Vegan", profile.getMealPreference());
        assertEquals(15, profile.getPoints());
        assertEquals(LoyaltyStatus.UPDATED, profile.getStatus());

        LoyaltyProfile compensatedProfile = new LoyaltyProfile("Sam", "Standard", 20, LoyaltyStatus.COMPENSATED);
        IllegalStateException blockedUpdate = assertThrows(IllegalStateException.class,
                () -> compensatedProfile.savePreference("Kosher"));
        assertTrue(blockedUpdate.getMessage().contains("Compensated profile"));
    }

    @Test
    void workflowGatewayAndAdminFacadeCoverEmptyAndReplacementPaths() {
        WorkflowGateway gateway = new WorkflowGateway(tempDir);
        assertNull(gateway.loadLatest());
        assertTrue(gateway.loadAll().isEmpty());

        WorkflowProgress first = new WorkflowProgress("wf-1", "Amina", "res-1", null, WorkflowStatus.PENDING_B);
        WorkflowProgress updated = new WorkflowProgress("wf-1", "Amina", "res-2", "ticket-1", WorkflowStatus.COMPLETED);
        gateway.save(first);
        gateway.save(updated);

        assertEquals(1, gateway.loadAll().size());
        assertEquals("ticket-1", gateway.loadLatest().getTicketId());
        assertEquals(WorkflowStatus.COMPLETED, gateway.loadLatest().getStatus());

        ReservationAdminFacade adminFacade = new ReservationAdminFacade(tempDir.resolve("admin-empty"));
        assertNull(adminFacade.inspectWorkflow());
        assertNull(adminFacade.manualCompensate());
        assertTrue(adminFacade.getReservations().isEmpty());
        assertTrue(adminFacade.getTickets().isEmpty());
        assertTrue(adminFacade.getProfiles().isEmpty());
    }

    @Test
    void facadeRecoveryAndGetterMethodsCoverRemainingBranches() {
        FlightReservationFacade facade = new FlightReservationFacade(tempDir);
        facade.book("Noura", "FZ-900", "30B", 13.0, "Standard");

        assertNotNull(facade.getWorkflowProgress());
        assertEquals(1, facade.getReservations().size());
        assertEquals(1, facade.getTickets().size());
        assertEquals(1, facade.getProfiles().size());
        assertEquals("Noura", facade.getProfile("Noura").getPassengerName());
        assertEquals(WorkflowStatus.COMPLETED, facade.recoverWorkflowProgress().getStatus());

        FlightReservationFacade emptyFacade = new FlightReservationFacade(tempDir.resolve("empty"));
        assertNull(emptyFacade.recoverWorkflowProgress());
    }
}