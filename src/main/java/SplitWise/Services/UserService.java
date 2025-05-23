package SplitWise.Services;

import ReadWriteLock.ReadWriteLock;
import SplitWise.Models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserService {

    private final List<User> userRepository;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock  readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    public UserService() {
        userRepository = new ArrayList<>();
    }

    public void addUser(User user) {
        writeLock.lock();
        try{
            if(userRepository.contains(user)){
                return;
            }
            userRepository.add(user);
        }finally {
            writeLock.unlock();
        }
    }
    public void removeUser(User user) {
        writeLock.lock();
        try {
            if(userRepository.contains(user)){
                userRepository.remove(user);
            }
        }finally {
            writeLock.unlock();
        }
    }
    public User getUser(String id) {
        readLock.lock();
        try {
            return userRepository
                    .stream()
                    .filter( user ->  user.userId().equals(id) )
                    .findFirst().orElse(null);
        }finally {
            readLock.unlock();
        }
    }

    public static class UserBuilder{
        private String name;
        private String email;
        private String number;
        public UserBuilder setName(String name){
            this.name = name;
            return this;
        }
        public UserBuilder setEmail(String email){
            this.email = email;
            return this;
        }
        public UserBuilder setNumber(String number){
            this.number = number;
            return this;
        }
        public User build(){
            return new User(UUID.randomUUID().toString(), name, email, number);
        }
    }

}
