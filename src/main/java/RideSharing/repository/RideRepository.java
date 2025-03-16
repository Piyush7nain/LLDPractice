package RideSharing.repository;


import RideSharing.exceptions.RideNotFoundException;
import RideSharing.models.Ride;
import RideSharing.models.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RideRepository {
    private Map<String, Ride> offeredRides = new HashMap<>();
    private Map<String, Ride> completedRides = new HashMap<>();
    private static RideRepository rideRepository;
    private RideRepository() {}
    public static RideRepository getInstance() {
        if (rideRepository == null) {
            rideRepository = new RideRepository();
        }
        return rideRepository;
    }

    public void addOrUpdateOfferedRide(Ride ride) {

        if(ride.getStatus().equals(Status.COMPLETE)){
            offeredRides.remove(ride.getId());
            return;
        }
        this.offeredRides.put(ride.getId(), ride);

    }
    public void addOrUpdateCompletedRide(Ride ride) {
        this.completedRides.put(ride.getId(), ride);
    }

    public Ride getOfferedRide(String rideId) {
        if (!offeredRides.containsKey(rideId)) {
            throw new RideNotFoundException("Ride with id " + rideId + " does not exist");
        }
        return offeredRides.get(rideId);
    }
    public Ride getCompletedRide(String rideId) {
        if (!completedRides.containsKey(rideId)) {
            throw new RideNotFoundException("Ride with id " + rideId + " does not exist");
        }
        return completedRides.get(rideId);
    }
    public List<Ride> getOfferedRides() {
        return new ArrayList<>(offeredRides.values());
    }
    public List<Ride> getCompletedRides() {
        return new ArrayList<>(completedRides.values());
    }


}
