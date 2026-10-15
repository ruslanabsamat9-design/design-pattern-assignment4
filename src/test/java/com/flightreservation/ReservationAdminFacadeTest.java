package com.flightreservation;

import com.flightreservation.facade.FlightReservationFacade;
import com.flightreservation.facade.ReservationAdminFacade;
import com.flightreservation.reservation.Reservation;
import com.flightreservation.reservation.ReservationStatus;
import com.flightreservation.reservation.SeatReservationService;
import com.flightreservation.ticket.ETicket;
import com.flightreservation.ticket.ETicketStatus;
import com.flightreservation.ticket.TicketService;
import com.flightreservation.workflow.WorkflowGateway;
import com.flightreservation.workflow.WorkflowProgress;
import com.flightreservation.workflow.WorkflowStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReservationAdminFacadeTest {

    @TempDir
    Path tempDir;

    @Test
    void adminFacadeInspectsStateAndCompensatesPendingWorkflow() {
        SeatReservationService seatService = new SeatReservationService(tempDir);
        TicketService ticketService = new TicketService(tempDir);
        WorkflowGateway workflowGateway = new WorkflowGateway(tempDir);

        Reservation reservation = seatService.reserveSeat("AdminUser", "FZ-210", "41D");
        ETicket ticket = ticketService.issueTicket(reservation.getId(), "AdminUser", 12.0);

        WorkflowProgress progress = new WorkflowProgress(
                "wf-1",
                "AdminUser",
                reservation.getId(),
                ticket.getId(),
                WorkflowStatus.PENDING_C
        );
        workflowGateway.save(progress);

        ReservationAdminFacade adminFacade = new ReservationAdminFacade(tempDir);

        assertEquals(WorkflowStatus.PENDING_C, adminFacade.inspectWorkflow().getStatus());
        assertEquals(1, adminFacade.getReservations().size());
        assertEquals(1, adminFacade.getTickets().size());

        WorkflowProgress recovered = adminFacade.manualCompensate();
        assertEquals(WorkflowStatus.COMPENSATED, recovered.getStatus());
        assertEquals(ReservationStatus.CANCELLED, seatService.findById(reservation.getId()).getStatus());
        assertEquals(ETicketStatus.CANCELLED, ticketService.findById(ticket.getId()).getStatus());
    }

    @Test
    void adminFacadeCanReadPersistedWorkflowAfterFailure() {
        FlightReservationFacade facade = new FlightReservationFacade(tempDir);
        facade.bookWithFailure("Reader", "FZ-220", "44B", 11.0, "Standard", "B");

        ReservationAdminFacade adminFacade = new ReservationAdminFacade(tempDir);
        WorkflowProgress progress = adminFacade.inspectWorkflow();

        assertNotNull(progress);
        assertEquals(WorkflowStatus.COMPENSATED, progress.getStatus());
        assertEquals(1, adminFacade.getReservations().size());
        assertEquals(ReservationStatus.CANCELLED, adminFacade.getReservations().get(0).getStatus());
    }
}
