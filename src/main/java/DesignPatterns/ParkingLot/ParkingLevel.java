package DesignPatterns.ParkingLot;

import DesignPatterns.ParkingLot.models.Slot;
import DesignPatterns.ParkingLot.models.Vehicle;
import DesignPatterns.ParkingLot.models.VehicleType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ParkingLevel {
    private final Integer parkingLevel;
    private final Map<String, Slot> slots;
    private final Map<VehicleType, Integer> availableSlots;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    public ParkingLevel(int parkingLevel, Map<VehicleType, Integer> floorPlan) {
        this.parkingLevel = parkingLevel;
        this.availableSlots = new HashMap<>();
        for (VehicleType type : VehicleType.values()) {
            availableSlots.put(type, floorPlan.get(type));
        }
        this.slots = new HashMap<>();
        ReentrantReadWriteLock parkingLock = new ReentrantReadWriteLock();
        this.readLock = parkingLock.readLock();
        this.writeLock = parkingLock.writeLock();
    }

    public boolean available(VehicleType type) {
        readLock.lock();
        try{
            if(availableSlots.containsKey(type)) {
                return availableSlots.get(type) > 0;
            }
            return false;
        }finally {
            readLock.unlock();
        }
    }
    public int getAvailable(VehicleType type) {
        readLock.lock();
        try {
            if(available(type)){
                return availableSlots.get(type);
            }
            return 0;
        }finally {
            readLock.unlock();
        }
    }

    public Optional<Slot> park(Vehicle vehicle) {
        writeLock.lock();
        try{
            Optional<Slot> optional = Optional.empty();
            Slot slot = new Slot(parkingLevel, vehicle.vehicleType(), vehicle.licensePlate());
            if(available(vehicle.vehicleType())&& !isParked(slot)){
                slots.put(vehicle.licensePlate(), slot);
                removeSlot(slot);
                optional = Optional.of(slot);
            }
            return optional;
        }finally {
            writeLock.unlock();
        }
    }

    public boolean unpark(Slot slot) {
        writeLock.lock();
        try{
            if(isParked(slot)) {
                slots.remove(slot.licensePlate());
                addSlot(slot);
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean isParked(Slot slot){
        readLock.lock();
        try{
            return slots.containsKey(slot.licensePlate()) && slots.get(slot.licensePlate()).vehicleType().equals(slot.vehicleType());
        }finally {
            readLock.unlock();
        }
    }
    public void addSlot(Slot slot) {
        writeLock.lock();
        try{
            if(slot==null) return;
            availableSlots.put(slot.vehicleType(), availableSlots.getOrDefault(slot.vehicleType(), 0) + 1);
        }finally {
            writeLock.unlock();
        }

    }

    public void removeSlot(Slot slot) {
        writeLock.lock();
        try{
            if(slot==null) return;
            if(availableSlots.get(slot.vehicleType())==0) return;
            availableSlots.put(slot.vehicleType(), availableSlots.getOrDefault(slot.vehicleType(), 0) - 1);
        } finally {
            writeLock.unlock();
        }

    }
}
