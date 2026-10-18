package com.flightreservation.ticket;
public class BaggageRules {
    public static final double MAX_KG=23.0;
    public void validate(double kg){
        if(kg<=0 || kg>MAX_KG) throw new IllegalArgumentException("Baggage must be between 0 and 23 kg");
    }
}
