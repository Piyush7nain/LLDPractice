package RideSharing.exceptions;

public class IllegalRideException extends RuntimeException {
    public IllegalRideException() {
    }
    public IllegalRideException(String message) {
        super(message);
    }
}
