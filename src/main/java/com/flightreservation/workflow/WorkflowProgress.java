package com.flightreservation.workflow;

public class WorkflowProgress {
    private String workflowId;
    private String passengerName;
    private String reservationId;
    private String ticketId;
    private WorkflowStatus status;

    public WorkflowProgress() {
    }

    public WorkflowProgress(String workflowId, String passengerName, String reservationId, String ticketId, WorkflowStatus status) {
        this.workflowId = workflowId;
        this.passengerName = passengerName;
        this.reservationId = reservationId;
        this.ticketId = ticketId;
        this.status = status;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }
}
