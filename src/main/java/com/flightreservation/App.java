package com.flightreservation;

import com.flightreservation.facade.*;
import com.flightreservation.workflow.WorkflowProgress;
import java.nio.file.Path;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Path dir=Path.of(args.length>0?args[0]:"demo-data");
        FlightReservationFacade facade=new FlightReservationFacade(dir);
        ReservationAdminFacade admin=new ReservationAdminFacade(facade);
        System.out.println("Data folder: "+dir.toAbsolutePath());
        facade.recover();
        try(Scanner sc=new Scanner(System.in)) {
            while(true) {
                System.out.println("\n=== Flight Reservation ===");
                System.out.println("1. Book a flight");
                System.out.println("2. Demo B failure (baggage 35 kg)");
                System.out.println("3. Demo C failure (meal Pizza)");
                System.out.println("4. Inspect saved state");
                System.out.println("5. Recover unfinished workflows");
                System.out.println("6. Reset data");
                System.out.println("0. Exit");
                System.out.print("Choose: ");
                if(!sc.hasNextLine()) return;
                String choice=sc.nextLine().trim();
                if(choice.equals("0")) return;
                if(choice.equals("4")) { show(facade); continue; }
                if(choice.equals("5")) { System.out.println("Recovered: "+admin.compensateStuck().size()); continue; }
                if(choice.equals("6")) { admin.reset(); System.out.println("Reset complete."); continue; }
                if(choice.equals("1")||choice.equals("2")||choice.equals("3")) {
                    try {
                        System.out.print("Passenger name: "); String p=sc.nextLine();
                        System.out.print("Flight number (e.g. FZ-101): "); String f=sc.nextLine();
                        System.out.print("Seat (e.g. 12A): "); String s=sc.nextLine();
                        double kg=23;
                        if(choice.equals("2")) kg=35;
                        else {
                            System.out.print("Baggage kg (max 23): "); kg=Double.parseDouble(sc.nextLine().replace(',','.'));
                        }
                        System.out.print("Meal (Standard/Vegetarian/Vegan/Kosher/Halal/Gluten-Free): ");
                        String meal=choice.equals("3")?"Pizza":sc.nextLine();
                        BookingReceipt receipt=facade.book(new BookingRequest(p,f,s,kg,meal));
                        System.out.println("Booking completed: "+receipt);
                    } catch(RuntimeException e) { System.out.println("Booking failed: "+e.getMessage()); }
                } else System.out.println("Invalid option.");
            }
        }
    }
    private static void show(FlightReservationFacade f) {
        System.out.println("\nReservations:");
        f.reservations().forEach(x->System.out.println(x.getId()+" | "+x.getPassenger()+" | "+x.getFlight()+" "+x.getSeat()+" | "+x.getStatus()));
        System.out.println("Tickets:");
        f.tickets().forEach(x->System.out.println(x.getId()+" | "+x.getPassenger()+" | "+x.getBaggageKg()+"kg | "+x.getStatus()));
        System.out.println("Loyalty:");
        f.profiles().forEach(x->System.out.println(x.getPassenger()+" | "+x.getMeal()+" | "+x.getPoints()+" points"));
        System.out.println("Workflows:");
        for(WorkflowProgress w:f.workflows()) System.out.println(w.getId()+" | "+w.getPassenger()+" | "+w.getStatus()+" | "+w.getFailure());
    }
}
