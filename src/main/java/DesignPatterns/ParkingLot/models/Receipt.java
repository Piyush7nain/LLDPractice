package DesignPatterns.ParkingLot.models;

public record Receipt(
        String TicketNumber,
        Vehicle vehicle,
        String entryTime,
        String exitTime,
        String timeSpent,
        double fee,
        String pricingStrategy) {
}
