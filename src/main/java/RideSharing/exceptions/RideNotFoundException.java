package RideSharing.exceptions;

public class RideNotFoundException extends RuntimeException{
    public RideNotFoundException(){
        super("Ride Not Found");
    }
    public RideNotFoundException(String message){
        super(message);
    }
}
