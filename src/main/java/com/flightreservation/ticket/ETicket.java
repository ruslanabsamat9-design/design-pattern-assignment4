package com.flightreservation.ticket;

public class ETicket {
    private String id, reservationId, passenger;
    private double baggageKg;
    private TicketStatus status;
    public ETicket(){}
    public ETicket(String id,String reservationId,String passenger,double baggageKg){
        this.id=id;this.reservationId=reservationId;this.passenger=passenger;this.baggageKg=baggageKg;this.status=TicketStatus.ISSUED;
    }
    public void voidTicket(){
        if(status!=TicketStatus.ISSUED) throw new IllegalStateException("Only issued tickets can be voided");
        status=TicketStatus.VOIDED;
    }
    public String getId(){return id;} public String getReservationId(){return reservationId;}
    public String getPassenger(){return passenger;} public double getBaggageKg(){return baggageKg;}
    public TicketStatus getStatus(){return status;}
}
