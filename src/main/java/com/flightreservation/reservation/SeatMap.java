package com.flightreservation.reservation;

import java.util.List;

public class SeatMap {
    public void validate(String seat) {
        if (seat == null || !seat.matches("([1-9]|[1-3][0-9]|4[0-5])[A-F]"))
            throw new IllegalArgumentException("Seat must be a row from 1-45 and a letter A-F, e.g. 12A");
    }
    public boolean isTaken(String flight, String seat, List<Reservation> reservations) {
        return reservations.stream().anyMatch(r -> r.getFlight().equalsIgnoreCase(flight)
                && r.getSeat().equalsIgnoreCase(seat) && r.getStatus() == ReservationStatus.CONFIRMED);
    }
}
