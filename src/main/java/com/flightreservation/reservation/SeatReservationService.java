package com.flightreservation.reservation;

import com.flightreservation.storage.JsonStore;
import java.util.UUID;

public class SeatReservationService {
    private final ReservationRepository repository;
    private final SeatMap seatMap = new SeatMap();
    public SeatReservationService(JsonStore store){repository=new ReservationRepository(store);}
    public Reservation reserve(String passenger, String flight, String seat) {
        if (passenger == null || passenger.isBlank() || flight == null || flight.isBlank())
            throw new IllegalArgumentException("Passenger and flight are required");
        seatMap.validate(seat);
        if (seatMap.isTaken(flight, seat, repository.findAll())) throw new IllegalArgumentException("Seat is already taken");
        Reservation r=new Reservation(UUID.randomUUID().toString(), passenger.trim(), flight.trim(), seat.toUpperCase());
        repository.save(r); return r;
    }
    public void cancel(String id) {
        Reservation r=repository.find(id).orElseThrow(()->new IllegalArgumentException("Reservation not found"));
        if(r.getStatus()!=ReservationStatus.CANCELLED){r.cancel(); repository.save(r);}
    }
    public java.util.List<Reservation> all(){return repository.findAll();}
}
