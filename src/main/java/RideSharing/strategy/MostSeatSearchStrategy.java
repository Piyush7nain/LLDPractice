package RideSharing.strategy;


import RideSharing.models.Ride;
import RideSharing.models.Status;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MostSeatSearchStrategy implements SearchStrategy {
    private final String origin;
    private final String destination;
    private final int requiredSeats;

    public MostSeatSearchStrategy(String origin, String destination, int requiredSeats) {
        this.origin = origin;
        this.destination = destination;
        this.requiredSeats = requiredSeats;
    }
    @Override
    public Optional<Ride> searchRide(List<Ride> rides) {
        return rides.stream()
                .filter(
                        r -> r.getStatus().equals(Status.OFFERED)
                                && r.getOrigin().equals(origin)
                                && r.getDestination().equals(destination)
                                && r.getRemainingSeats()>=requiredSeats
                ).max(Comparator.comparingInt(Ride::getRemainingSeats));
    }
}
