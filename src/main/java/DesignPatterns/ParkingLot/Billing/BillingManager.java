package DesignPatterns.ParkingLot.Billing;

import DesignPatterns.ParkingLot.models.Receipt;
import DesignPatterns.ParkingLot.models.Ticket;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class BillingManager {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @Setter
    @Getter
    private BillingStrategy billingStrategy;

    public BillingManager(BillingStrategy billingStrategy) {
        this.billingStrategy = billingStrategy;
    }
    public Receipt getReceipt(Ticket ticket){
        String startTime = ticket.entryTime();
        String endTime = addTime(startTime);
        String timeDiff = getTimeDifference(startTime, endTime);
        double fee = billingStrategy.getBillingReceipt(startTime, endTime, ticket.slot().vehicleType());
        return new Receipt(
                ticket.id(),
                ticket.slot().vehicleType(),
                ticket.slot().licensePlate(),
                startTime,
                endTime,
                timeDiff,
                fee,
                billingStrategy.getBillingStrategy()
        );
    }

    public static String getTime(){
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        return now.format(formatter);
    }
    public static String addTime(String startTime){
        LocalDateTime parsedTime = LocalDateTime.parse(startTime, formatter);

        int minMinutes = 20;
        int maxMinutes = 5 * 60; // 5 hours in minutes
        int randomMinutesToAdd = ThreadLocalRandom.current().nextInt(minMinutes, maxMinutes + 1);

        LocalDateTime newTime = parsedTime.plusMinutes(randomMinutesToAdd);
        return newTime.format(formatter);
    }

    public static String getTimeDifference(String startTime, String endTime){
        Duration duration = Duration.between(LocalDateTime.parse(startTime, formatter),LocalDateTime.parse(endTime, formatter));
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + "hr : " + minutes + " mins";
    }
}
