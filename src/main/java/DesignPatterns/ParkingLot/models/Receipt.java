package DesignPatterns.ParkingLot.models;

public record Receipt(
        String TicketNumber,
        VehicleType vehicleType,
        String licensePlate,
        String entryTime,
        String exitTime,
        String timeSpent,
        double fee,
        String pricingStrategy) {
}
