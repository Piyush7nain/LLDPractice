package RideSharing.exceptions;

public class NoRideFoundException extends RuntimeException {
    public NoRideFoundException() {
        super("No Ride Found");
    }
    public NoRideFoundException(String message) {
        super(message);
    }
}
