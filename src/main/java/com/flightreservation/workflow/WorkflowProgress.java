package com.flightreservation.workflow;

public class WorkflowProgress {
    private String id, passenger, reservationId, ticketId, failure;
    private WorkflowStatus status;
    public WorkflowProgress(){}
    public WorkflowProgress(String id,String passenger,String reservationId,WorkflowStatus status){
        this.id=id;this.passenger=passenger;this.reservationId=reservationId;this.status=status;
    }
    public String getId(){return id;} public String getPassenger(){return passenger;}
    public String getReservationId(){return reservationId;} public String getTicketId(){return ticketId;}
    public String getFailure(){return failure;} public WorkflowStatus getStatus(){return status;}
    public void setTicketId(String value){ticketId=value;} public void setStatus(WorkflowStatus value){status=value;}
    public void setFailure(String value){failure=value;}
}
