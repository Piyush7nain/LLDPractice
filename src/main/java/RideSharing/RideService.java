package RideSharing;

import RideSharing.exceptions.IllegalRideException;
import RideSharing.exceptions.NoRideFoundException;
import RideSharing.models.Ride;
import RideSharing.models.Status;
import RideSharing.models.Vehicle;
import RideSharing.models.VehicleType;
import RideSharing.repository.RideRepository;
import RideSharing.strategy.SearchStrategy;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RideService {
    private static RideService rideService;
    private final RideRepository rideRepository;
    private final UserService userService;
    private RideService(RideRepository rideRepository, UserService userService) {
        this.rideRepository = rideRepository;
        this.userService = userService;
    }
    public static RideService getInstance(RideRepository rideRepository, UserService userService) {
        if(rideService == null){
            return rideService = new RideService(rideRepository, userService);
        }
        return rideService;
    }
    public List<Ride> getAllOfferedRides() {
        return rideRepository.getOfferedRides();
    }
    public List<Ride> getAllCompletedRides() {
        return rideRepository.getCompletedRides();
    }
    public List<Ride> getAllRides() {
        List<Ride> allRides = rideRepository.getCompletedRides();
        allRides.addAll(rideRepository.getOfferedRides());
        return allRides;
    }

    public String createRide(String userName,
                           Vehicle vehicle,
                           String origin,
                           String destination,
                           int seats) throws IllegalRideException {
        Ride ride = new Ride(userName, vehicle, origin, destination, seats);
        if(existingRide(ride)){
            throw new IllegalRideException("Ride from user "+ride.getOwnerName() +" and with vehicle "+ ride.getVehicle().getNumber() +" already exists");
        }else{
            rideRepository.addOrUpdateOfferedRide(ride);
            userService.getUser(userName).addOfferedRides(ride.getId());
        }
        return ride.getId();
    }

    public List<Ride> getRidesByUser(String username, List<Ride> rides) {
        return rides.stream().filter((ride) -> ride.getOwnerName().equals(username)).collect(Collectors.toList());
    }
    public List<Ride> getRidesByVehicle(VehicleType vehicleType, List<Ride> rides) {
        return rides.stream().filter(
                        (ride) -> ride.getVehicle()
                                .getVehicleType()
                                .equals(vehicleType))
                .collect(Collectors.toList());
    }
    private boolean existingRide(Ride ride){
        List<Ride> rides = getRidesByVehicle(ride.getVehicle().getVehicleType(),
                getRidesByUser(ride.getOwnerName(), rideRepository.getOfferedRides()));
        return !rides.isEmpty();
    }

    public String selectRide(String passengerName,  SearchStrategy searchStrategy) {
        // find appropriate ride;
        Optional<Ride> maybeRide = searchStrategy.searchRide(rideService.getAllOfferedRides());
        if (!maybeRide.isPresent()) {
            throw new NoRideFoundException("No Rides founds");
        }
        // Add passenger to ride
        Ride ride = maybeRide.get();
        ride.addPassenger(passengerName);
        userService.getUser(passengerName).addTakenRides(ride.getId());
        return ride.getId();

    }

    public void endRide(String rideId){
        Ride ride = rideRepository.getOfferedRide(rideId);
        ride.setStatus(Status.COMPLETE);
        rideRepository.addOrUpdateOfferedRide(ride);
        rideRepository.addOrUpdateCompletedRide(ride);

    }

    public void removeRide(String rideId){
        Ride ride = rideRepository.getOfferedRide(rideId);
        if(ride==null){
            throw new NoRideFoundException("No Rides found");
        }
        ride.setStatus(Status.DELETED);
        rideRepository.addOrUpdateOfferedRide(ride);
    }

    public Ride getRide(String rideId){
        return rideRepository.getOfferedRide(rideId);
    }
}
