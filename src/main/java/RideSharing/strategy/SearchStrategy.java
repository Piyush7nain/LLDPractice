package RideSharing.strategy;


import RideSharing.models.Ride;

import java.util.List;
import java.util.Optional;

public interface SearchStrategy {

    Optional<Ride> searchRide(List<Ride> rides);
}
