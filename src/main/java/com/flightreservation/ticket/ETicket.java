package com.flightreservation.ticket;

public class ETicket {
    private String id;
    private String reservationId;
    private String passengerName;
    private double baggageKg;
    private ETicketStatus status;

    public ETicket() {
    }

    public ETicket(String id, String reservationId, String passengerName, double baggageKg, ETicketStatus status) {
        this.id = id;
        this.reservationId = reservationId;
        this.passengerName = passengerName;
        this.baggageKg = baggageKg;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public double getBaggageKg() {
        return baggageKg;
    }

    public void setBaggageKg(double baggageKg) {
        this.baggageKg = baggageKg;
    }

    public ETicketStatus getStatus() {
        return status;
    }

    public void setStatus(ETicketStatus status) {
        this.status = status;
    }

    public void issue() {
        if (status == ETicketStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled ticket cannot be issued again");
        }
        this.status = ETicketStatus.ISSUED;
    }

    public void cancel() {
        if (status == ETicketStatus.CANCELLED) {
            throw new IllegalStateException("Ticket is already cancelled");
        }
        this.status = ETicketStatus.CANCELLED;
    }
}
