package com.flightreservation.facade;

import com.flightreservation.workflow.WorkflowProgress;
import java.util.List;

public class ReservationAdminFacade {
    private final FlightReservationFacade booking;
    public ReservationAdminFacade(FlightReservationFacade booking){this.booking=booking;}
    public List<WorkflowProgress> inspectWorkflows(){return booking.workflows();}
    public List<WorkflowProgress> compensateStuck(){return booking.recover();}
    public void reset(){booking.reset();}
}
