package com.flightreservation.facade;

public record BookingRequest(String passenger,String flight,String seat,double baggageKg,String meal) {
    public void validate(){
        if(passenger==null || passenger.isBlank()) throw new IllegalArgumentException("Passenger name is required");
        if(flight==null || flight.isBlank()) throw new IllegalArgumentException("Flight number is required");
    }
}
