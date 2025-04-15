package DesignPatterns.ParkingLot.Billing;

import DesignPatterns.ParkingLot.models.VehicleType;
import lombok.Getter;

import java.util.Map;

public class FlatPriceBillingStrategy implements BillingStrategy {
    private final int flatPrice;
    private final Map<VehicleType, Double> vehiclePriceMultiplier;
    private final BillingStrategyType billingStrategyType = BillingStrategyType.FLAT;
    public FlatPriceBillingStrategy(int flatPrice, Map<VehicleType, Double> vehiclePriceMultiplier) {
        this.flatPrice = flatPrice;
        this.vehiclePriceMultiplier = vehiclePriceMultiplier;
    }

    @Override
    public double getBillingReceipt(String startTime, String endTime, VehicleType vehicleType) {
        return flatPrice* vehiclePriceMultiplier.get(vehicleType);
    }
    @Override
    public String getBillingStrategy(){
        return billingStrategyType.toString();
    }
}
