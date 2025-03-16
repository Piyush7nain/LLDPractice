package RideSharing.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ride {
    final private String id;
    final private String ownerName;
    final private Vehicle vehicle;
    final private String origin;
    final private String destination;
    final private int seats;
    private int bookedSeats;
    private List<String> passengers;
    private Status status;

    public Ride(String ownerName,Vehicle vehicle, String origin, String destination, int seats) {
        this.id = UUID.randomUUID().toString();
        this.ownerName = ownerName;
        this.vehicle = vehicle;
        this.origin = origin;
        this.destination = destination;
        this.seats = seats;
        this.bookedSeats = 0;
        this.passengers = new ArrayList<>();
        this.status = Status.OFFERED;
    }

    public void addPassenger(String passenger) {
        this.passengers.add(passenger);
        bookedSeats++;
        if(bookedSeats == seats) {
            status = Status.BOOKED;
        }
    }
    public void removePassenger(User passenger) {
        this.passengers.remove(passenger);
        bookedSeats--;
        if(bookedSeats < seats) {
            status = Status.OFFERED;
        }
    }
    public String getOwnerName() {
        return ownerName;
    }
    public String getOrigin() {
        return origin;
    }
    public String getDestination() {
        return destination;
    }
    public int getSeats() {
        return seats;
    }
    public int getBookedSeats() {
        return bookedSeats;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public String getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
    public int getRemainingSeats() {
        return seats - bookedSeats;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ride [");
        sb.append(id);
        sb.append(", ownerName=");
        sb.append(ownerName);
        sb.append(", vehicle=");
        sb.append(vehicle.toString());
        sb.append(", origin=");
        sb.append(origin);
        sb.append(", destination=");
        sb.append(destination);
        sb.append(", seats Offerred=");
        sb.append(seats);
        sb.append(", bookedSeats=");
        sb.append(bookedSeats);
        sb.append(", status=");
        sb.append(status);
        sb.append(", passenger List=");
        sb.append(passengers);
        sb.append("]");

        return sb.toString();
    }

}
