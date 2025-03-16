package RideSharing;

public class TripManager {
    private final RideService rideService;
    private final UserService userService;
    public TripManager(RideService rideService, UserService userService) {
        this.rideService = rideService;
        this.userService = userService;
    }


}
