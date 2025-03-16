package RideSharing.strategy;


import RideSharing.models.Ride;
import RideSharing.models.Status;

import java.util.List;
import java.util.Optional;

public class PreferredVehicleSearchStrategy implements SearchStrategy {

    private final String origin;
    private final String destination;
    private final int requiredSeats;
    private final String preferredVehicleType;
    public PreferredVehicleSearchStrategy(String origin, String destination, int requiredSeats, String preferredVehicleType) {
        this.origin = origin;
        this.destination = destination;
        this.requiredSeats = requiredSeats;
        this.preferredVehicleType = preferredVehicleType;
    }
    @Override
    public Optional<Ride> searchRide(List<Ride> rides) {
        return rides.stream().filter(
                r -> r.getStatus().equals(Status.OFFERED)
                        && r.getOrigin().equals(origin)
                        && r.getDestination().equals(destination)
                        && r.getRemainingSeats()>=requiredSeats
                        && r.getVehicle().getVehicleType().toString().equals(preferredVehicleType)
        ).findFirst();
    }
}
