package DesignPatterns.ParkingLot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record Ticket(String id, String entryTime, Slot slot) {
}
