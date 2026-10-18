package com.flightreservation.loyalty;

public class LoyaltyProfile {
    private String passenger, meal;
    private int points;
    private LoyaltyStatus status;
    public LoyaltyProfile(){}
    public LoyaltyProfile(String passenger,String meal,int points){
        this.passenger=passenger;this.meal=meal;this.points=points;this.status=LoyaltyStatus.ACTIVE;
    }
    public String getPassenger(){return passenger;} public String getMeal(){return meal;}
    public int getPoints(){return points;} public LoyaltyStatus getStatus(){return status;}
    public void close(){
        if(status!=LoyaltyStatus.ACTIVE) throw new IllegalStateException("Only active profiles can be closed");
        status=LoyaltyStatus.CLOSED;
    }
}
