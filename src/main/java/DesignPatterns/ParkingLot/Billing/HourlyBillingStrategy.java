package DesignPatterns.ParkingLot.Billing;

import DesignPatterns.ParkingLot.models.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class HourlyBillingStrategy implements  BillingStrategy{
    private final Map<VehicleType, Double> vehiclePriceMultiplier;
    private final double hourlyRate;
    private BillingStrategyType billingStrategyType = BillingStrategyType.HOURLY;
    public HourlyBillingStrategy(double hourlyRate, Map<VehicleType, Double> vehiclePriceMultiplier) {
        this.hourlyRate = hourlyRate;
        this.vehiclePriceMultiplier = vehiclePriceMultiplier;
    }
    @Override
    public double getBillingReceipt(String startTime, String endTime, VehicleType vehicleType) {
        Duration duration = Duration.between(LocalDateTime.parse(startTime, BillingManager.formatter),LocalDateTime.parse(endTime, BillingManager.formatter));
        long hours = duration.toHours()+1;
        return vehiclePriceMultiplier.get(vehicleType)*hourlyRate*hours;
    }
    @Override
    public String getBillingStrategy(){
        return billingStrategyType.toString();
    }
}
