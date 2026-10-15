package com.flightreservation;

import com.flightreservation.facade.FlightReservationFacade;
import com.flightreservation.loyalty.LoyaltyProfile;
import com.flightreservation.reservation.Reservation;
import com.flightreservation.workflow.WorkflowProgress;
import com.flightreservation.workflow.WorkflowStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FlightReservationPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void persistedDataSurvivesRestart() {
        FlightReservationFacade firstFacade = new FlightReservationFacade(tempDir);
        Reservation reservation = firstFacade.book("Nursultan", "FZ-202", "14C", 18.0, "Kosher");

        WorkflowProgress workflow = firstFacade.getWorkflowProgress();
        assertNotNull(workflow);
        assertEquals(WorkflowStatus.COMPLETED, workflow.getStatus());
        assertEquals(reservation.getId(), workflow.getReservationId());

        FlightReservationFacade reopenedFacade = new FlightReservationFacade(tempDir);

        Reservation reloadedReservation = reopenedFacade.getReservations().get(0);
        assertEquals("Nursultan", reloadedReservation.getPassengerName());
        assertEquals("14C", reloadedReservation.getSeatNumber());

        LoyaltyProfile profile = reopenedFacade.getProfile("Nursultan");
        assertEquals("Kosher", profile.getMealPreference());
        assertEquals(10, profile.getPoints());

        WorkflowProgress reloadedWorkflow = reopenedFacade.getWorkflowProgress();
        assertEquals(WorkflowStatus.COMPLETED, reloadedWorkflow.getStatus());
        assertEquals(reloadedReservation.getId(), reloadedWorkflow.getReservationId());
    }
}
