package DesignPatterns.ParkingLot;

import DesignPatterns.ParkingLot.Billing.FlatPriceBillingStrategy;
import DesignPatterns.ParkingLot.Billing.HourlyBillingStrategy;
import DesignPatterns.ParkingLot.models.Receipt;
import DesignPatterns.ParkingLot.models.Slot;
import DesignPatterns.ParkingLot.models.Ticket;
import DesignPatterns.ParkingLot.models.Vehicle;
import DesignPatterns.ParkingLot.models.VehicleType;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParkingLotTest {

    private ParkingLot parkingLot;

    @Before
    public void setup() {

        parkingLot = new ParkingLot("Main Lot", 2, Map.of(
                VehicleType.CAR, 2,
                VehicleType.BIKE, 2,
                VehicleType.ELECTRIC, 1
        ), new FlatPriceBillingStrategy(100,Map.of(
                VehicleType.CAR, 2.0,
                VehicleType.BIKE, 1.0,
                VehicleType.ELECTRIC, 2.5))
        );
    }

    @Test
    public void testFullCycleForCar() throws InterruptedException {
        Vehicle car = new Vehicle("KA-01-1234", VehicleType.CAR);
        Optional<Ticket> ticket = parkingLot.parkVehicle(car);
        assertTrue(ticket.isPresent());
        assertEquals(car.vehicleType(), ticket.get().slot().vehicleType());
        assertEquals(car.licensePlate(), ticket.get().slot().licensePlate());
        assertNotNull(ticket.get().slot());

        Thread.sleep(100); // simulate short stay
        Optional<Receipt> receipt = parkingLot.unparkVehicle(ticket.get());
        assertTrue(receipt.isPresent());
        assertEquals(car.vehicleType(), receipt.get().vehicleType());
        assertEquals(car.licensePlate(), receipt.get().licensePlate());
        assertTrue(receipt.get().fee() >= 0);
    }

    @Test
    public void testFullCycleForBike() {
        Vehicle bike = new Vehicle("KA-02-1111", VehicleType.BIKE);
        Optional<Ticket> ticket = parkingLot.parkVehicle(bike);
        assertTrue(ticket.isPresent());
        assertEquals(bike.vehicleType(), ticket.get().slot().vehicleType());
        assertEquals(bike.licensePlate(), ticket.get().slot().licensePlate());

        Optional<Receipt> receipt = parkingLot.unparkVehicle(ticket.get());
        assertEquals(bike.vehicleType(), receipt.get().vehicleType());
        assertEquals(bike.licensePlate(), receipt.get().licensePlate());
        assertTrue(receipt.get().fee() >= 0);
    }


    @Test
    public void testSlotReleaseOnExit() {
        Vehicle car1 = new Vehicle("KA-01-0001", VehicleType.CAR);
        Vehicle car2 = new Vehicle("KA-01-0002", VehicleType.CAR);

        Optional<Ticket> ticket1 = parkingLot.parkVehicle(car1);
        assertTrue(ticket1.isPresent());

        Optional<Receipt> receipt = parkingLot.unparkVehicle(ticket1.get());
        assertTrue(receipt.isPresent());

        Optional<Ticket> ticket2 = parkingLot.parkVehicle(car2);
        assertTrue( "Slot should be available after car1 exits", ticket2.isPresent());
    }

    @Test
    public void testEVSlotAssignmentAndFallback() {
        Vehicle ev = new Vehicle("KA-EV-1234", VehicleType.ELECTRIC);
        Vehicle ev2 = new Vehicle("KA-EV-5678", VehicleType.ELECTRIC);
        Optional<Ticket> ticket1 = parkingLot.parkVehicle(ev);
        Optional<Ticket> ticket2 = parkingLot.parkVehicle(ev2);
        assertTrue(ticket1.isPresent());
        assertTrue(ticket2.isPresent());

        Vehicle ev3 = new Vehicle("KA-EV-5678", VehicleType.ELECTRIC);
        Optional<Ticket> ticket3 = parkingLot.parkVehicle(ev3);
        assertFalse( "Should reject EV if no EV slot is left",ticket3.isPresent());
    }

    @Test
    public void testTicketAndReceiptAccuracy() throws InterruptedException {
        Vehicle car = new Vehicle("KA-03-3333", VehicleType.CAR);
        Optional<Ticket> ticket = parkingLot.parkVehicle(car);
        assertEquals(car.vehicleType(), ticket.get().slot().vehicleType());
        assertEquals(car.licensePlate(), ticket.get().slot().licensePlate());
        assertNotNull(ticket.get().entryTime());
        assertNotNull(ticket.get().slot());

        Thread.sleep(100);
        Optional<Receipt> receipt = parkingLot.unparkVehicle(ticket.get());
        assertEquals(car.licensePlate(), receipt.get().licensePlate());
        assertEquals(car.vehicleType(), receipt.get().vehicleType());
        assertNotNull(receipt.get().entryTime());
        assertNotNull(receipt.get().exitTime());
        assertTrue(receipt.get().fee() > 0);
    }

    @Test
    public void testParkingFullScenario() {
        parkingLot.parkVehicle(new Vehicle("C1", VehicleType.CAR));
        parkingLot.parkVehicle(new Vehicle("C2", VehicleType.CAR));
        parkingLot.parkVehicle(new Vehicle("C3", VehicleType.CAR));
        parkingLot.parkVehicle(new Vehicle("C4", VehicleType.CAR));
        Optional<Ticket> ticket = parkingLot.parkVehicle(new Vehicle("C5", VehicleType.CAR));
        assertFalse("Should not assign slot when parking is full", ticket.isPresent());
    }

    @Test
    public void testSlotAvailabilityAfterExit() {
        Vehicle bike = new Vehicle("B1", VehicleType.BIKE);
        Optional<Ticket> ticket = parkingLot.parkVehicle(bike);
        parkingLot.unparkVehicle(ticket.get());

        assertFalse( "Slot should be free after bike exits",parkingLot.isFull(VehicleType.BIKE));
    }

    @Test
    public void testInvalidUnparkAttempt() {
        Vehicle car = new Vehicle("KA-05-1111", VehicleType.CAR);
        LocalDateTime date = LocalDateTime.now();
        Ticket ticket = new Ticket(UUID.randomUUID().toString(),date.toLocalTime().toString(), new Slot(1, car.vehicleType(), car.licensePlate()));
        Optional<Receipt> receipt = parkingLot.unparkVehicle(ticket);
        assertFalse( "Unparking with invalid ticket should fail", receipt.isPresent());
    }

    @Test
    public void testAdminViewAllSlotsStatus() {
        Map<VehicleType, Integer> availableSlots = parkingLot.viewSlotAvailability();
        assertNotNull(availableSlots);
        assertTrue(availableSlots.get(VehicleType.CAR) >= 0);
        assertTrue(availableSlots.get(VehicleType.BIKE) >= 0);
    }

    @Test
    public void testHourlyFeeStrategy() throws InterruptedException {
        Vehicle car = new Vehicle("KA-10-1001", VehicleType.CAR);
        parkingLot.setBillingStrategy(new HourlyBillingStrategy(50, Map.of(
                VehicleType.CAR, 2.0,
                VehicleType.BIKE, 1.0,
                VehicleType.ELECTRIC, 2.5)));
        Optional<Ticket> ticket = parkingLot.parkVehicle(car);
        Thread.sleep(2000); // simulate 2 seconds stay
        Optional<Receipt> receipt = parkingLot.unparkVehicle(ticket.get());

        double expectedMinFee = 20.0; // 1 hour min charge
        assertTrue(receipt.get().fee() >= expectedMinFee);
    }

    @Test
    public void testFlatFeeStrategy() {
        Vehicle bike = new Vehicle("KA-10-1002", VehicleType.BIKE);
        parkingLot.setBillingStrategy(new FlatPriceBillingStrategy(50, Map.of(
                VehicleType.CAR, 2.0,
                VehicleType.BIKE, 1.0,
                VehicleType.ELECTRIC, 2.5)));
        Optional<Ticket> ticket = parkingLot.parkVehicle(bike);
        Optional<Receipt> receipt = parkingLot.unparkVehicle(ticket.get());

        assertEquals(50.0, receipt.get().fee(), 0.1);
    }


}

