package RideSharing.models;

public class Vehicle {
    final private VehicleType vehicleType;
    final private String number;
    public Vehicle(VehicleType vehicleType, String number) {
        this.vehicleType = vehicleType;
        this.number = number;
    }
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(vehicleType.toString());
        sb.append(", ");
        sb.append(number);
        sb.append("]");
        return sb.toString();
    }
}
