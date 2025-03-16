package RideSharing.repository;



import RideSharing.exceptions.UserNotFoundException;
import RideSharing.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private static UserRepository userRepository;
    Map<String, User> users;
    private UserRepository() {
        users = new HashMap<>();
    }

    public static UserRepository getInstance() {
        if (userRepository == null) {
            userRepository = new UserRepository();
        }
        return userRepository;
    }

    public void addUser(User user) {
        users.put(user.getName(), user);
    }
    public User getUser(String userName) {
        if(!users.containsKey(userName)) {
            throw new UserNotFoundException();
        }
        return users.get(userName);
    }
    public User updateUser(User user) {
        if(!users.containsKey(user.getName())) {
            throw new UserNotFoundException();
        }
        users.put(user.getName(), user);
        return user;
    }
    public void removeUser(String userName) {
        users.remove(userName);
    }
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
