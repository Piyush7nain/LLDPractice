package RideSharing;


import RideSharing.models.User;
import RideSharing.models.Vehicle;
import RideSharing.repository.UserRepository;

import java.util.List;

public class UserService {
    private static UserService userService;
    private UserRepository userRepository;
    private UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public static UserService getInstance(UserRepository userRepository) {
        if(userService == null) {
            userService = new UserService(userRepository);
        }
        return userService;
    }

    public void createUser(User user) {
        userRepository.addUser(user);
    }
    public User getUser(String username) {
        return userRepository.getUser(username);
    }
    public void updateUser(User user) {
        userRepository.updateUser(user);
    }
    public void deleteUser(String username) {
        userRepository.removeUser(username);
    }

    public void registerVehicle(String username, Vehicle vehicle) {
        User user = userRepository.getUser(username);
        List<Vehicle> vehicles = user.getVehicles();
        vehicles.add(vehicle);
        user.addVehicles(vehicles);
    }
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

}
