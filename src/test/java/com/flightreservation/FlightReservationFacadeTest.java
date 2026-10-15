package com.flightreservation;

import com.flightreservation.facade.FlightReservationFacade;
import com.flightreservation.loyalty.LoyaltyProfile;
import com.flightreservation.reservation.Reservation;
import com.flightreservation.reservation.ReservationStatus;
import com.flightreservation.reservation.SeatReservationService;
import com.flightreservation.ticket.ETicket;
import com.flightreservation.ticket.ETicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FlightReservationFacadeTest {

    @TempDir
    Path tempDir;

    @Test
    void normalWorkflowCreatesReservationTicketAndLoyaltyProfile() {
        FlightReservationFacade facade = new FlightReservationFacade(tempDir);

        Reservation reservation = facade.book("Amina", "FZ-101", "12A", 20.0, "Vegetarian");

        assertNotNull(reservation);
        assertEquals("Amina", reservation.getPassengerName());
        assertEquals("FZ-101", reservation.getFlightNumber());
        assertEquals("12A", reservation.getSeatNumber());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());

        assertEquals(1, facade.getReservations().size());

        ETicket ticket = facade.getTickets().get(0);
        assertEquals(ETicketStatus.ISSUED, ticket.getStatus());
        assertEquals(reservation.getId(), ticket.getReservationId());
        assertEquals(20.0, ticket.getBaggageKg());

        LoyaltyProfile profile = facade.getProfile("Amina");
        assertEquals("Vegetarian", profile.getMealPreference());
        assertEquals(10, profile.getPoints());
    }

    @Test
    void bookingFailsAtBAndCompensatesReservation() {
        FlightReservationFacade facade = new FlightReservationFacade(tempDir);

        Reservation reservation = facade.bookWithFailure("Bora", "FZ-103", "22B", 10.0, "Standard", "B");

        assertNotNull(reservation);
        assertEquals(ReservationStatus.CANCELLED, facade.getReservations().get(0).getStatus());
        assertEquals("COMPENSATED", facade.getWorkflowProgress().getStatus().name());
    }

    @Test
    void bookingFailsAtCAndCancelsTicketAndSeat() {
        FlightReservationFacade facade = new FlightReservationFacade(tempDir);

        bookingFailsAtCAndCancelsTicketAndSeat(facade);
    }

    @Test
    void duplicateSeatReservationFailsBeforeFurtherCommit() {
        SeatReservationService service = new SeatReservationService(tempDir);
        service.reserveSeat("First", "FZ-110", "99Z");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.reserveSeat("Second", "FZ-110", "99Z")
        );

        assertTrue(exception.getMessage().contains("Seat is already reserved"));
        assertEquals(1, service.getReservations().size());
    }

    private void bookingFailsAtCAndCancelsTicketAndSeat(FlightReservationFacade facade) {
        Reservation reservation = facade.bookWithFailure("Selma", "FZ-104", "30A", 15.0, "Vegan", "C");

        assertNotNull(reservation);
        assertEquals(1, facade.getReservations().size());
        assertEquals(ReservationStatus.CANCELLED, facade.getReservations().get(0).getStatus());
        assertEquals(1, facade.getTickets().size());
        assertEquals(ETicketStatus.CANCELLED, facade.getTickets().get(0).getStatus());
        assertEquals("COMPENSATED", facade.getWorkflowProgress().getStatus().name());
    }
}
