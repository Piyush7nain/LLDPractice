package RideSharing.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    final private String name;
    final private String gender;
    final private int age;
    private List<Vehicle> vehicles;
    private List<String> offeredRides;
    private List<String> takenRides;
    public User(String name, String gender, int age) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.vehicles = new ArrayList<>();
        this.offeredRides = new ArrayList<>();
        this.takenRides = new ArrayList<>();
    }

    public User(String name, String gender, int age, List<Vehicle> car) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.vehicles = car;
        this.offeredRides = new ArrayList<>();
        this.takenRides = new ArrayList<>();
    }
    public String getName() {
        return name;
    }
    public String getGender() {
        return gender;
    }
    public int getAge() {
        return age;
    }
    public List<Vehicle> getVehicles() {
        return vehicles;
    }
    public void addVehicles(List<Vehicle> vehicles) {
        this.vehicles.addAll(vehicles);
    }
    public Vehicle getVehicle(String number){
        for(Vehicle vehicle : vehicles){
            if(vehicle.getNumber().equals(number)){
                return vehicle;
            }
        }
        return null;
    }
    public List<String> getOfferedRides() {
        return offeredRides;
    }
    public void addOfferedRides(String offeredRide) {
        if(!this.offeredRides.contains(offeredRide)){
            this.offeredRides.add(offeredRide);
        }
    }
    public List<String> getTakenRides() {
        return takenRides;
    }
    public void addTakenRides(String takenRide) {
        if(!this.takenRides.contains(takenRide)){
            this.takenRides.add(takenRide);
        }
    }

}
