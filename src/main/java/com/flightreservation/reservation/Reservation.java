package com.flightreservation.reservation;

public class Reservation {
    private String id, passenger, flight, seat;
    private ReservationStatus status;
    public Reservation() {}
    public Reservation(String id, String passenger, String flight, String seat) {
        this.id=id; this.passenger=passenger; this.flight=flight; this.seat=seat; this.status=ReservationStatus.CONFIRMED;
    }
    public void cancel() {
        if (status != ReservationStatus.CONFIRMED) throw new IllegalStateException("Only confirmed reservations can be cancelled");
        status = ReservationStatus.CANCELLED;
    }
    public String getId(){return id;} public String getPassenger(){return passenger;}
    public String getFlight(){return flight;} public String getSeat(){return seat;}
    public ReservationStatus getStatus(){return status;}
}
