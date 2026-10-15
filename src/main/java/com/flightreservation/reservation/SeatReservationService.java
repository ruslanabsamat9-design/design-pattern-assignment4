package com.flightreservation.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class SeatReservationService {
    private final Path dataFile;
    private final ObjectMapper objectMapper;

    public SeatReservationService(Path dataDirectory) {
        this.objectMapper = new ObjectMapper();
        this.dataFile = dataDirectory.resolve("reservations.json");
        try {
            Files.createDirectories(dataDirectory);
            if (Files.notExists(dataFile)) {
                Files.writeString(dataFile, "[]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize reservation storage", e);
        }
    }

    public Reservation reserveSeat(String passengerName, String flightNumber, String seatNumber) {
        List<Reservation> reservations = loadReservations();

        boolean alreadyTaken = reservations.stream()
                .anyMatch(r -> r.getSeatNumber().equalsIgnoreCase(seatNumber)
                        && r.getFlightNumber().equalsIgnoreCase(flightNumber)
                        && r.getStatus() == ReservationStatus.CONFIRMED);

        if (alreadyTaken) {
            throw new IllegalArgumentException("Seat is already reserved: " + seatNumber);
        }

        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                passengerName,
                flightNumber,
                seatNumber,
                ReservationStatus.PENDING
        );
        reservation.confirm();

        reservations.add(reservation);
        saveReservations(reservations);
        return reservation;
    }

    public Reservation cancelReservation(String reservationId) {
        List<Reservation> reservations = loadReservations();
        Reservation reservation = reservations.stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.cancel();
        saveReservations(reservations);
        return reservation;
    }

    public List<Reservation> getReservations() {
        return loadReservations();
    }

    public Reservation findById(String reservationId) {
        return loadReservations().stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
    }

    private List<Reservation> loadReservations() {
        try {
            return objectMapper.readValue(dataFile.toFile(), new TypeReference<List<Reservation>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not read reservation file", e);
        }
    }

    private void saveReservations(List<Reservation> reservations) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), reservations);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write reservation file", e);
        }
    }
}
