package com.flightreservation;

import com.flightreservation.facade.FlightReservationFacade;
import com.flightreservation.facade.ReservationAdminFacade;
import com.flightreservation.reservation.Reservation;
import com.flightreservation.ticket.ETicket;
import com.flightreservation.workflow.WorkflowProgress;
import com.flightreservation.workflow.WorkflowStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final Path DATA_DIR = Path.of("demo-data");

    public static void main(String[] args) {
        FlightReservationFacade facade = new FlightReservationFacade(DATA_DIR);
        ReservationAdminFacade adminFacade = new ReservationAdminFacade(DATA_DIR);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleNormalBooking(facade, scanner);
                    break;
                case "2":
                    handleFailure(facade, scanner, "B");
                    break;
                case "3":
                    handleFailure(facade, scanner, "C");
                    break;
                case "4":
                    inspectState(facade, adminFacade);
                    break;
                case "5":
                    triggerAdminCompensation(adminFacade);
                    break;
                case "6":
                    resetDemoData();
                    System.out.println("Demo data cleared.");
                    break;
                case "0":
                    System.out.println("Exiting flight reservation demo.");
                    return;
                default:
                    System.out.println("Please choose a valid menu option.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== Flight Reservation Demo ===");
        System.out.println("1. Normal reservation");
        System.out.println("2. Trigger B-stage failure");
        System.out.println("3. Trigger C-stage failure");
        System.out.println("4. Inspect persisted state");
        System.out.println("5. Manual compensation via admin facade");
        System.out.println("6. Reset demo data");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void handleNormalBooking(FlightReservationFacade facade, Scanner scanner) {
        System.out.print("Passenger name: ");
        String passenger = scanner.nextLine();
        System.out.print("Flight number: ");
        String flight = scanner.nextLine();
        System.out.print("Seat number: ");
        String seat = scanner.nextLine();
        System.out.print("Baggage kg: ");
        double baggage = Double.parseDouble(scanner.nextLine());
        System.out.print("Meal preference: ");
        String meal = scanner.nextLine();

        try {
            Reservation reservation = facade.book(passenger, flight, seat, baggage, meal);
            System.out.println("Reservation created successfully: " + reservation.getId());
            System.out.println("Workflow status: " + facade.getWorkflowProgress().getStatus());
        } catch (Exception e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private static void handleFailure(FlightReservationFacade facade, Scanner scanner, String stage) {
        System.out.print("Passenger name: ");
        String passenger = scanner.nextLine();
        System.out.print("Flight number: ");
        String flight = scanner.nextLine();
        System.out.print("Seat number: ");
        String seat = scanner.nextLine();
        System.out.print("Baggage kg: ");
        double baggage = Double.parseDouble(scanner.nextLine());
        System.out.print("Meal preference: ");
        String meal = scanner.nextLine();

        try {
            Reservation reservation = facade.bookWithFailure(passenger, flight, seat, baggage, meal, stage);
            System.out.println("Failure scenario executed for stage " + stage + ".");
            System.out.println("Final reservation status: " + reservation.getStatus());
            System.out.println("Workflow status: " + facade.getWorkflowProgress().getStatus());
        } catch (Exception e) {
            System.out.println("Failure simulation error: " + e.getMessage());
        }
    }

    private static void inspectState(FlightReservationFacade facade, ReservationAdminFacade adminFacade) {
        List<Reservation> reservations = facade.getReservations();
        List<ETicket> tickets = facade.getTickets();
        WorkflowProgress progress = adminFacade.inspectWorkflow();

        System.out.println("Reservations:");
        for (Reservation reservation : reservations) {
            System.out.println(" - " + reservation.getId() + " | " + reservation.getPassengerName() + " | "
                    + reservation.getFlightNumber() + " | " + reservation.getSeatNumber() + " | " + reservation.getStatus());
        }

        System.out.println("Tickets:");
        for (ETicket ticket : tickets) {
            System.out.println(" - " + ticket.getId() + " | " + ticket.getReservationId() + " | " + ticket.getStatus());
        }

        if (progress == null) {
            System.out.println("No workflow progress found.");
        } else {
            System.out.println("Workflow: " + progress.getWorkflowId() + " | " + progress.getStatus());
        }
    }

    private static void triggerAdminCompensation(ReservationAdminFacade adminFacade) {
        WorkflowProgress progress = adminFacade.inspectWorkflow();
        if (progress == null) {
            System.out.println("There is no persisted workflow to compensate.");
            return;
        }

        if (progress.getStatus() == WorkflowStatus.COMPLETED || progress.getStatus() == WorkflowStatus.COMPENSATED) {
            System.out.println("This workflow is already complete or compensated.");
            return;
        }

        WorkflowProgress result = adminFacade.manualCompensate();
        System.out.println("Manual compensation finished. Status: " + result.getStatus());
    }

    private static void resetDemoData() {
        try {
            if (Files.exists(DATA_DIR)) {
                deleteDirectory(DATA_DIR);
            }
        } catch (IOException e) {
            System.out.println("Could not clear demo data: " + e.getMessage());
        }
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (Files.isDirectory(directory)) {
            try (var paths = Files.walk(directory)) {
                paths.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
