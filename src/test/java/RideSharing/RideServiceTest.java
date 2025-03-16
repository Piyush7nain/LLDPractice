package RideSharing;

import RideSharing.models.Ride;
import RideSharing.models.Status;
import RideSharing.models.User;
import RideSharing.models.Vehicle;
import RideSharing.models.VehicleType;
import RideSharing.repository.RideRepository;
import RideSharing.repository.UserRepository;
import RideSharing.strategy.MostSeatSearchStrategy;
import RideSharing.strategy.SearchStrategy;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class RideServiceTest {
    private RideService rideService;
    private UserService userService;
    private RideRepository rideRepository;
    private UserRepository userRepository;
    @Before
    public void setUp() {
        userRepository = UserRepository.getInstance();
        userService = UserService.getInstance(userRepository);
        rideRepository = RideRepository.getInstance();
        rideService = RideService.getInstance(rideRepository, userService);
        AddUsersAndVehicales();
    }

    @Test
    public void testAddRide() {
        User rohan  = userService.getUser("Rohan");
        rideService.createRide(rohan.getName(),rohan.getVehicle("KA-01-12345"), "Hyderabad", "Bangalore", 1);
        assertEquals(1, rideService.getAllRides().size());
    }

    @Test
    public void testRemoveRide() {
        User rohan  = userService.getUser("Rohan");
        String rideId = rideService.createRide(rohan.getName(),rohan.getVehicle("KA-01-12345"), "Hyderabad", "Bangalore", 1);
        rideService.removeRide(rideId);
        assertEquals(rideService.getRide(rideId).getStatus(), Status.DELETED);
    }

//    @Test
//    public void testSearchByMostAvailableSeats() {
//        User rohan  = userService.getUser("Rohan");
//        rideService.createRide(rohan.getName(),rohan.getVehicle("KA-01-12345"), "Hyderabad", "Bangalore", 1);
//        User shipra = userService.getUser("Shipra");
//        rideService.createRide(shipra.getName(), shipra.getVehicle("KA-04-12345"), "Bangalore", "Mysore", 1);
//        rideService.createRide(shipra.getName(), shipra.getVehicle("KA-03-12345"), "Bangalore", "Mysore", 2);
//        SearchStrategy searchStrategy = new MostSeatSearchStrategy("Bangalore", "Mysore",  1 );
//
//        List<Ride> results = rideService.searchRides( searchStrategy);
//
//        assertEquals(2, results.size());
////        assertEquals("2", results.get(0).getRideId());  // Ride with 5 seats first
//    }
/*
    @Test
    public void testSearchByPreferredVehicle() {
        Ride ride1 = new Ride("1", "A", "B", 3, "Car", "John");
        Ride ride2 = new Ride("2", "A", "B", 5, "Bike", "Mike");
        rideService.addRide(ride1);
        rideService.addRide(ride2);

        rideService.setSearchStrategy(new PreferredVehicleStrategy("Car"));
        List<Ride> results = rideService.searchRides("A", "B");

        assertEquals(1, results.size());
        assertEquals("Car", results.get(0).getVehicleType());
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(10);

        Runnable addTask = () -> {
            rideService.addRide(new Ride(UUID.randomUUID().toString(), "A", "B", 4, "Car", "User"));
            latch.countDown();
        };

        Runnable removeTask = () -> {
            if (!rideService.getAllRides().isEmpty()) {
                rideService.removeRide(rideService.getAllRides().get(0).getRideId());
            }
            latch.countDown();
        };

        for (int i = 0; i < 5; i++) {
            executor.submit(addTask);
            executor.submit(removeTask);
        }

        latch.await();
        executor.shutdown();

        assertTrue(rideService.getAllRides().size() >= 0); // No concurrency issues
    }*/

    private void AddUsersAndVehicales() {
        User rohan = new User("Rohan", "M", 36);
        userService.createUser(rohan);
        User shashank = new User("Shashank", "M", 29);
        userService.createUser(shashank);
        User nandini = new User("Nandini", "M", 28);
        userService.createUser(nandini);
        User shipra = new User("Shipra", "M", 27);
        userService.createUser(shipra);
        User gaurav = new User("Gaurav", "M", 26);
        userService.createUser(gaurav);
        User rahul = new User("Rahul", "M", 35);
        userService.createUser(rahul);

        //Add vehicle
        Vehicle v1 = new Vehicle(VehicleType.SWIFT, "KA-01-12345");
        userService.registerVehicle(userService.getUser("Rohan").getName(), v1);
        Vehicle v2 = new Vehicle(VehicleType.BALENO, "KA-02-12345");
        userService.registerVehicle(userService.getUser("Shashank").getName(), v2);
        Vehicle v3 = new Vehicle(VehicleType.POLO, "KA-03-12345");
        Vehicle v4 = new Vehicle(VehicleType.ACTIVA, "KA-04-12345");
        userService.registerVehicle(userService.getUser("Shipra").getName(), v3);
        userService.registerVehicle(userService.getUser("Shipra").getName(), v4);
        Vehicle v5 = new Vehicle(VehicleType.XUV, "KA-05-12345");
        userService.registerVehicle(userService.getUser("Rahul").getName(), v5);
    }
}
