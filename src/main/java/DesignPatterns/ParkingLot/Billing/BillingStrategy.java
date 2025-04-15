package DesignPatterns.ParkingLot.Billing;

import DesignPatterns.ParkingLot.models.Receipt;
import DesignPatterns.ParkingLot.models.Ticket;
import DesignPatterns.ParkingLot.models.VehicleType;

public interface BillingStrategy {
    double getBillingReceipt(String startTime, String endTime, VehicleType vehicleType);
    String getBillingStrategy();
}
