package DesignPatterns.ParkingLot;

import DesignPatterns.ParkingLot.Billing.BillingManager;
import DesignPatterns.ParkingLot.Billing.BillingStrategy;
import DesignPatterns.ParkingLot.models.Receipt;
import DesignPatterns.ParkingLot.models.Slot;
import DesignPatterns.ParkingLot.models.Ticket;
import DesignPatterns.ParkingLot.models.Vehicle;
import DesignPatterns.ParkingLot.models.VehicleType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class ParkingLot {

    private final String parkingLotName;
    private final List<ParkingLevel> parkingLevels;
    private final BillingManager billingManager;
    public ParkingLot(String parkingLotName, int levels, Map<VehicleType, Integer> floorMap, BillingStrategy billingStrategy) {
        this.parkingLotName = parkingLotName;
        this.parkingLevels =  new ArrayList<>();
        for(int level = 0; level < levels; level++) {
            ParkingLevel level1 = new ParkingLevel(level, floorMap);
            parkingLevels.add(level1);
        }
        this.billingManager = new BillingManager(billingStrategy);

    }

    public Optional<Ticket> parkVehicle(Vehicle vehicle) {
        Optional<Slot> slot = Optional.empty();
        Optional<Ticket> optionalTicket = Optional.empty();
        boolean parked = false;
        int floor = 0;
        while(floor < parkingLevels.size() && !parked) {
            slot = parkingLevels.get(floor).park(vehicle);
            if(slot.isPresent()) {
                parked = true;
            }
            floor++;
        }
        if(slot.isPresent()) {
            Ticket ticket = new Ticket(UUID.randomUUID().toString(), BillingManager.getTime(), slot.get());
            optionalTicket = Optional.of(ticket);
        }
        return optionalTicket;
    }
    public Optional<Receipt> unparkVehicle(Ticket ticket) {
        Slot slot = ticket.slot();
        if(slot == null || slot.parkingLevel()>=parkingLevels.size()) {
            return Optional.empty();
        }
        if(parkingLevels.isEmpty()) {
            return Optional.empty();
        }
        Optional<Receipt> receipt = Optional.empty();
        if(parkingLevels.get(ticket.slot().parkingLevel()).unpark(slot)){
            Receipt rcpt = billingManager.getReceipt(ticket);
            receipt = Optional.of(rcpt);
        }
        return receipt;
    }

    public Map<VehicleType, Integer> viewSlotAvailability() {
        Map<VehicleType, Integer> availability = new HashMap<>();
        for(ParkingLevel level : parkingLevels) {
            for(VehicleType type : VehicleType.values()) {
                availability.put(type, availability.getOrDefault(type,0)+ level.getAvailable(type));
            }
        }
        return availability;
    }

    public boolean isFull(VehicleType vehicleType) {
        for(ParkingLevel parkingLevel : parkingLevels) {
            if(parkingLevel.available(vehicleType)){
                return false;
            }
        }
        return true;
    }
    public void setBillingStrategy(BillingStrategy billingStrategy) {
        billingManager.setBillingStrategy(billingStrategy);
    }
}
