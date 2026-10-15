package com.flightreservation.ticket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class TicketService {
    private final Path dataFile;
    private final ObjectMapper objectMapper;

    public TicketService(Path dataDirectory) {
        this.objectMapper = new ObjectMapper();
        this.dataFile = dataDirectory.resolve("tickets.json");
        try {
            Files.createDirectories(dataDirectory);
            if (Files.notExists(dataFile)) {
                Files.writeString(dataFile, "[]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize ticket storage", e);
        }
    }

    public ETicket issueTicket(String reservationId, String passengerName, double baggageKg) {
        if (baggageKg > 23.0) {
            throw new IllegalArgumentException("Baggage exceeds free allowance");
        }

        List<ETicket> tickets = loadTickets();
        ETicket ticket = new ETicket(
                UUID.randomUUID().toString(),
                reservationId,
                passengerName,
                baggageKg,
                ETicketStatus.PENDING
        );
        ticket.issue();
        tickets.add(ticket);
        saveTickets(tickets);
        return ticket;
    }

    public ETicket cancelTicket(String ticketId) {
        List<ETicket> tickets = loadTickets();
        ETicket ticket = tickets.stream()
                .filter(t -> t.getId().equals(ticketId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        ticket.cancel();
        saveTickets(tickets);
        return ticket;
    }

    public List<ETicket> getTickets() {
        return loadTickets();
    }

    public ETicket findById(String ticketId) {
        return loadTickets().stream()
                .filter(t -> t.getId().equals(ticketId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
    }

    private List<ETicket> loadTickets() {
        try {
            return objectMapper.readValue(dataFile.toFile(), new TypeReference<List<ETicket>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not read ticket file", e);
        }
    }

    private void saveTickets(List<ETicket> tickets) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), tickets);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write ticket file", e);
        }
    }
}
