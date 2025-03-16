package RideSharing;

import RideSharing.models.Ride;
import RideSharing.models.User;
import RideSharing.models.Vehicle;
import RideSharing.models.VehicleType;
import RideSharing.repository.RideRepository;
import RideSharing.repository.UserRepository;
import RideSharing.strategy.MostSeatSearchStrategy;
import RideSharing.strategy.PreferredVehicleSearchStrategy;
import RideSharing.strategy.SearchStrategy;

import java.util.ArrayList;
import java.util.List;

public class RideSharing {


    public static UserRepository userRepository = UserRepository.getInstance();
    public static UserService userService = UserService.getInstance(userRepository);
    public static RideRepository rideRepository = RideRepository.getInstance();
    public static RideService rideService = RideService.getInstance(rideRepository, userService);
    public static List<String> currentRides = new ArrayList<>();
    public static void main(String[] args) {

        AddUsersAndVehicales();
        OfferRides();
        selectRides();
        printRides();
        endRides();
//        printRides();
        printUserStates();
    }
    private static void printUserStates() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            System.out.println(user.getName() + "  Taken Rides ["+user.getTakenRides().size()+"]"+" Offerred Rides ["+user.getOfferedRides().size()+"]");
        }
    }
    private static void endRides(){
        for(String ride : currentRides){
            rideService.endRide(ride);
        }
    }
    private static void printRides(){
        for(Ride ride: rideService.getAllRides()){
            System.out.println(ride);
        }
    }
    private static void selectRides(){
        try {
            //select_ride(“Nandini, Origin=Bangalore, Destination=Mysore, Seats=1, Most Vacant”)
            SearchStrategy s1 = new MostSeatSearchStrategy("Bangalore", "Mysore", 1);
            String NandiniRide = rideService.selectRide("Nandini", s1);
            System.out.println("Nandini's Ride Id is : " + NandiniRide);
            currentRides.add(NandiniRide);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try{
            //select_ride(“Gaurav, Origin=Bangalore, Destination=Mysore, Seats=1, Preferred Vehicle=Activa”)
            SearchStrategy s2 = new PreferredVehicleSearchStrategy("Bangalore", "Mysore", 1 , VehicleType.ACTIVA.toString());
            String GauravRide = rideService.selectRide("Gaurav", s2 );
            System.out.println("Gaurav's Ride Id is : "+ GauravRide);
            currentRides.add(GauravRide);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try{
            //select_ride(“Shashank, Origin=Mumbai, Destination=Bangalore, Seats=1, Most Vacant”)
            SearchStrategy s3 = new MostSeatSearchStrategy("Mumbai", "Bangalore", 1);
            String shashankRide = rideService.selectRide("Shashank", s3 );
            System.out.println("Shashank's Ride Id is : "+ shashankRide);
            currentRides.add(shashankRide);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try{
            //select_ride(“Rohan, Origin=Hyderabad, Destination=Bangalore, Seats=1, Preferred Vehicle=Baleno”)
            SearchStrategy s4 = new PreferredVehicleSearchStrategy("Hyderabad", "Bangalore", 1 , VehicleType.BALENO.toString());
            String rohanRide = rideService.selectRide("Rohan", s4 );
            System.out.println("Rohan's Ride Id is : "+ rohanRide);
            currentRides.add(rohanRide);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try{
            //select_ride(“Shashank, Origin=Hyderabad, Destination=Bangalore, Seats=1,Preferred Vehicle=Polo”)
            SearchStrategy s5 = new PreferredVehicleSearchStrategy("Hyderabad", "Bangalore", 1, VehicleType.POLO.toString());
            String shashankRide = rideService.selectRide("Shashank", s5 );
            System.out.println("Shashank's Ride Id is : "+ shashankRide);
            currentRides.add(shashankRide);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    private static void OfferRides() {
        try{
            User rohan  = userService.getUser("Rohan");
            rideService.createRide(rohan.getName(),rohan.getVehicle("KA-01-12345"), "Hyderabad", "Bangalore", 1);
            User shipra = userService.getUser("Shipra");
            rideService.createRide(shipra.getName(), shipra.getVehicle("KA-04-12345"), "Bangalore", "Mysore", 1);
            rideService.createRide(shipra.getName(), shipra.getVehicle("KA-03-12345"), "Bangalore", "Mysore", 2);
            User shashank = userService.getUser("Shashank");
            rideService.createRide(shashank.getName(), shashank.getVehicle("KA-02-12345"), "Hyderabad", "Bangalore", 2);
            User rahul = userService.getUser("Rahul");
            rideService.createRide(rahul.getName(), rahul.getVehicle("KA-05-12345"), "Hyderabad", "Bangalore", 5);
            rideService.createRide(rohan.getName(), rohan.getVehicle("KA-01-12345"), "Bangalore", "Pune", 1);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    private static void AddUsersAndVehicales() {
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
