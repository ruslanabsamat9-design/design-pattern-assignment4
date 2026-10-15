package com.flightreservation.loyalty;

public class LoyaltyProfile {
    private String passengerName;
    private String mealPreference;
    private int points;
    private LoyaltyStatus status;

    public LoyaltyProfile() {
    }

    public LoyaltyProfile(String passengerName, String mealPreference, int points, LoyaltyStatus status) {
        this.passengerName = passengerName;
        this.mealPreference = mealPreference;
        this.points = points;
        this.status = status;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getMealPreference() {
        return mealPreference;
    }

    public void setMealPreference(String mealPreference) {
        this.mealPreference = mealPreference;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public LoyaltyStatus getStatus() {
        return status;
    }

    public void setStatus(LoyaltyStatus status) {
        this.status = status;
    }

    public void savePreference(String mealPreference) {
        if (this.status == LoyaltyStatus.COMPENSATED) {
            throw new IllegalStateException("Compensated profile cannot be updated");
        }
        this.mealPreference = mealPreference;
        this.status = LoyaltyStatus.UPDATED;
        this.points += 10;
    }
}
