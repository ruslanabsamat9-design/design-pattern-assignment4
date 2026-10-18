package com.flightreservation;

import com.flightreservation.facade.*;
import com.flightreservation.workflow.WorkflowStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class FlightReservationFacadeTest {
    @TempDir Path temp;
    @Test void successfulBookingCompletesAllSteps() {
        FlightReservationFacade f=new FlightReservationFacade(temp);
        BookingReceipt r=f.book(new BookingRequest("Aruzhan","FZ-101","12A",20,"Halal"));
        assertNotNull(r.ticketId());
        assertEquals(WorkflowStatus.COMPLETED,f.workflows().get(0).getStatus());
        assertEquals(1,f.reservations().size());
        assertEquals(1,f.tickets().size());
    }
    @Test void baggageFailureCancelsReservation() {
        FlightReservationFacade f=new FlightReservationFacade(temp);
        assertThrows(IllegalStateException.class,()->f.book(new BookingRequest("Aruzhan","FZ-101","12A",35,"Halal")));
        assertEquals("CANCELLED",f.reservations().get(0).getStatus().name());
        assertEquals(WorkflowStatus.COMPENSATED,f.workflows().get(0).getStatus());
        assertTrue(f.tickets().isEmpty());
    }
    @Test void mealFailureVoidsTicketAndCancelsReservation() {
        FlightReservationFacade f=new FlightReservationFacade(temp);
        assertThrows(IllegalStateException.class,()->f.book(new BookingRequest("Aruzhan","FZ-101","12A",20,"Pizza")));
        assertEquals("CANCELLED",f.reservations().get(0).getStatus().name());
        assertEquals("VOIDED",f.tickets().get(0).getStatus().name());
        assertEquals(WorkflowStatus.COMPENSATED,f.workflows().get(0).getStatus());
    }
    @Test void dataSurvivesNewFacadeInstance() {
        new FlightReservationFacade(temp).book(new BookingRequest("Aruzhan","FZ-101","12A",20,"Halal"));
        FlightReservationFacade restarted=new FlightReservationFacade(temp);
        assertEquals(1,restarted.reservations().size());
        assertEquals(1,restarted.tickets().size());
    }
}
