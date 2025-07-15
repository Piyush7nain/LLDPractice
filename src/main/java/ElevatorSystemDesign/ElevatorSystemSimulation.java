package ElevatorSystemDesign;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// --- Enums ---

enum Direction {
    UP, DOWN, IDLE
}

enum ElevatorState {
    STOPPED, MOVING, DOORS_OPEN
}

// --- Interfaces ---

interface ElevatorObserver {
    void update(Elevator elevator);
    void onElevatorArrival(Elevator elevator, int floor);
    void onElevatorDoorsOpen(Elevator elevator, int floor);
    void onElevatorDoorsClose(Elevator elevator, int floor);
    void onElevatorDirectionChange(Elevator elevator, Direction newDirection);
}

// --- Core Classes ---

class Elevator {
    @Getter
    private final int id;
    @Getter
    private int currentFloor;
    @Getter
    private Direction direction;
    @Getter
    private ElevatorState state;
    @Getter
    private int currentCapacity;
    @Getter
    private final int maxCapacity;

    // Use TreeSet for sorted order of requests
    @Getter
    private final TreeSet<Integer> internalRequests; // Destination floors requested inside the elevator
    @Getter
    private final Set<Integer> externalUpRequestsToServe; // Floors this elevator is assigned to serve going UP
    @Getter
    private final Set<Integer> externalDownRequestsToServe; // Floors this elevator is assigned to serve going DOWN

    private final List<ElevatorObserver> observers;
    private final ExecutorService elevatorExecutor; // For elevator's own thread

    public Elevator(int id, int maxCapacity) {
        this.id = id;
        this.currentFloor = 0; // Starting floor
        this.direction = Direction.IDLE;
        this.state = ElevatorState.STOPPED;
        this.currentCapacity = 0;
        this.maxCapacity = maxCapacity;
        this.internalRequests = new TreeSet<>();
        this.externalUpRequestsToServe = ConcurrentHashMap.newKeySet(); // Thread-safe set
        this.externalDownRequestsToServe = ConcurrentHashMap.newKeySet(); // Thread-safe set
        this.observers = new CopyOnWriteArrayList<>(); // Thread-safe list
        this.elevatorExecutor = Executors.newSingleThreadExecutor();
        startProcessingRequests();
    }

    public Set<Integer> getInternalRequests() {
        return Collections.unmodifiableSet(internalRequests);
    }

    public boolean isFull() {
        return currentCapacity >= maxCapacity;
    }

    public void addObserver(ElevatorObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ElevatorObserver observer) {
        observers.remove(observer);
    }

    private void notifyObserversUpdate() {
        for (ElevatorObserver observer : observers) {
            observer.update(this);
        }
    }

    private void notifyArrival(int floor) {
        for (ElevatorObserver observer : observers) {
            observer.onElevatorArrival(this, floor);
        }
    }

    private void notifyDoorsOpen(int floor) {
        for (ElevatorObserver observer : observers) {
            observer.onElevatorDoorsOpen(this, floor);
        }
    }

    private void notifyDoorsClose(int floor) {
        for (ElevatorObserver observer : observers) {
            observer.onElevatorDoorsClose(this, floor);
        }
    }

    private void notifyDirectionChange(Direction newDirection) {
        for (ElevatorObserver observer : observers) {
            observer.onElevatorDirectionChange(this, newDirection);
        }
    }

    public synchronized void addInternalRequest(int floor) {
        if (floor < Building.MIN_FLOOR || floor > Building.MAX_FLOOR) {
            System.out.println("Elevator " + id + ": Invalid internal request for floor " + floor);
            return;
        }
        if (!internalRequests.contains(floor)) {
            internalRequests.add(floor);
            System.out.println("Elevator " + id + ": Added internal request for floor " + floor);
            if (direction == Direction.IDLE) {
                // If idle, immediately set direction towards the request
                updateDirection();
            }
            notifyObserversUpdate();
        }
    }

    // Called by ElevatorControlSystem to assign an external request
    public synchronized void assignExternalRequest(int floor, Direction reqDirection) {
        if (floor < Building.MIN_FLOOR || floor > Building.MAX_FLOOR) {
            System.out.println("Elevator " + id + ": Invalid external request assignment for floor " + floor);
            return;
        }

        boolean added = false;
        if (reqDirection == Direction.UP) {
            added = externalUpRequestsToServe.add(floor);
        } else {
            added = externalDownRequestsToServe.add(floor);
        }

        if (added) {
            System.out.println("Elevator " + id + ": Assigned external " + reqDirection + " request for floor " + floor);
            if (direction == Direction.IDLE) {
                // If idle, immediately set direction towards the request
                updateDirection();
            }
            notifyObserversUpdate();
        }
    }

    private void openDoors() {
        state = ElevatorState.DOORS_OPEN;
        System.out.println("Elevator " + id + " at floor " + currentFloor + ": Doors Opening.");
        notifyDoorsOpen(currentFloor);
        try {
            Thread.sleep(1000); // Simulate door opening time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeDoors() {
        System.out.println("Elevator " + id + " at floor " + currentFloor + ": Doors Closing.");
        notifyDoorsClose(currentFloor);
        try {
            Thread.sleep(1000); // Simulate door closing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        state = ElevatorState.STOPPED;
    }

    private void move() {
        if (direction == Direction.UP) {
            currentFloor++;
        } else if (direction == Direction.DOWN) {
            currentFloor--;
        }
        state = ElevatorState.MOVING;
        System.out.println("Elevator " + id + ": Moving " + direction + " to floor " + currentFloor);
        notifyObserversUpdate();
        try {
            Thread.sleep(2000); // Simulate travel time between floors
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Logic to update elevator's direction based on pending requests
    private synchronized void updateDirection() {
        int highestInternalReq = internalRequests.isEmpty() ? Building.MIN_FLOOR : internalRequests.last();
        int lowestInternalReq = internalRequests.isEmpty() ? Building.MAX_FLOOR : internalRequests.first();

        int highestExternalUpReq = externalUpRequestsToServe.isEmpty() ? Building.MIN_FLOOR : externalUpRequestsToServe.stream().max(Integer::compare).orElse(Building.MIN_FLOOR);
        int lowestExternalDownReq = externalDownRequestsToServe.isEmpty() ? Building.MAX_FLOOR : externalDownRequestsToServe.stream().min(Integer::compare).orElse(Building.MAX_FLOOR);


        Direction oldDirection = this.direction;

        if (direction == Direction.UP) {
            // Still have requests above current floor in UP direction
            if (internalRequests.stream().anyMatch(f -> f >= currentFloor) || externalUpRequestsToServe.stream().anyMatch(f -> f >= currentFloor)) {
                return; // Continue UP
            }
            // No more UP requests above or at current floor, check if there are DOWN requests anywhere
            if (!internalRequests.isEmpty() || !externalDownRequestsToServe.isEmpty()) {
                this.direction = Direction.DOWN;
            } else {
                this.direction = Direction.IDLE;
            }
        } else if (direction == Direction.DOWN) {
            // Still have requests below current floor in DOWN direction
            if (internalRequests.stream().anyMatch(f -> f <= currentFloor) || externalDownRequestsToServe.stream().anyMatch(f -> f <= currentFloor)) {
                return; // Continue DOWN
            }
            // No more DOWN requests below or at current floor, check if there are UP requests anywhere
            if (!internalRequests.isEmpty() || !externalUpRequestsToServe.isEmpty()) {
                this.direction = Direction.UP;
            } else {
                this.direction = Direction.IDLE;
            }
        } else { // IDLE state
            // Prioritize requests that are 'closest' or align with a natural flow
            if (!internalRequests.isEmpty()) {
                if (currentFloor < highestInternalReq) {
                    this.direction = Direction.UP;
                } else if (currentFloor > lowestInternalReq) {
                    this.direction = Direction.DOWN;
                } else { // At a floor with an internal request, but potentially no clear direction yet
                    // This scenario is handled by processRequests stopping. If more internal requests exist,
                    // direction will be set. If current floor is the only internal request, it might go idle.
                    // For simplicity, let's assume if an internal request exists, it gives a direction.
                    if (internalRequests.size() > 1) { // If multiple internal requests
                        if (internalRequests.last() > currentFloor) {
                            this.direction = Direction.UP;
                        } else if (internalRequests.first() < currentFloor) {
                            this.direction = Direction.DOWN;
                        }
                    } else if (internalRequests.first() != currentFloor) { // If only one internal request not at current floor
                        if (internalRequests.first() > currentFloor) {
                            this.direction = Direction.UP;
                        } else {
                            this.direction = Direction.DOWN;
                        }
                    } else { // Only one internal request at current floor, should go idle after processing
                        this.direction = Direction.IDLE;
                    }
                }
            } else if (!externalUpRequestsToServe.isEmpty() && currentFloor < highestExternalUpReq) {
                this.direction = Direction.UP;
            } else if (!externalDownRequestsToServe.isEmpty() && currentFloor > lowestExternalDownReq) {
                this.direction = Direction.DOWN;
            } else if (!externalUpRequestsToServe.isEmpty()) { // If no clear direction from external requests, but some exist
                this.direction = Direction.UP; // Default to UP if any UP request
            } else if (!externalDownRequestsToServe.isEmpty()) {
                this.direction = Direction.DOWN; // Default to DOWN if any DOWN request
            } else {
                this.direction = Direction.IDLE; // No requests
            }
        }

        if (oldDirection != this.direction) {
            notifyDirectionChange(this.direction);
            System.out.println("Elevator " + id + ": Direction changed from " + oldDirection + " to " + this.direction);
        }
    }


    // The main loop for elevator operation
    private void startProcessingRequests() {
        elevatorExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (this) {
                    if (internalRequests.isEmpty() && externalUpRequestsToServe.isEmpty() && externalDownRequestsToServe.isEmpty()) {
                        if (direction != Direction.IDLE) {
                            direction = Direction.IDLE;
                            notifyDirectionChange(Direction.IDLE);
                        }
                        try {
                            // No requests, wait for a new one
                            this.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    }

                    updateDirection();

                    boolean shouldStop = false;

                    if (direction == Direction.UP) {
                        if (internalRequests.contains(currentFloor)) {
                            shouldStop = true;
                        } else if (externalUpRequestsToServe.contains(currentFloor)) {
                            shouldStop = true;
                        }
                    } else if (direction == Direction.DOWN) {
                        if (internalRequests.contains(currentFloor)) {
                            shouldStop = true;
                        } else if (externalDownRequestsToServe.contains(currentFloor)) {
                            shouldStop = true;
                        }
                    } else { // IDLE but potentially has requests to move to
                        if (internalRequests.contains(currentFloor) ||
                                externalUpRequestsToServe.contains(currentFloor) ||
                                externalDownRequestsToServe.contains(currentFloor)) {
                            shouldStop = true;
                        }
                    }

                    if (shouldStop) {
                        openDoors();
                        System.out.println("Elevator " + id + ": Arrived at floor " + currentFloor);
                        notifyArrival(currentFloor);

                        // Drop off internal requests
                        if (internalRequests.remove(currentFloor)) {
                            System.out.println("Elevator " + id + ": Dropped off passenger at floor " + currentFloor);
                            currentCapacity--; // Simulate passenger exiting
                        }

                        // Pick up external requests if capacity allows and direction matches
                        if (direction == Direction.UP && externalUpRequestsToServe.contains(currentFloor)) {
                            // Simulate passenger entering
                            if (!isFull()) {
                                externalUpRequestsToServe.remove(currentFloor);
                                currentCapacity++;
                                System.out.println("Elevator " + id + ": Picked up UP request at floor " + currentFloor);
                            } else {
                                System.out.println("Elevator " + id + ": Full, cannot pick up UP request at " + currentFloor);
                                // The ECS needs to be re-notified or handle this
                            }
                        } else if (direction == Direction.DOWN && externalDownRequestsToServe.contains(currentFloor)) {
                            // Simulate passenger entering
                            if (!isFull()) {
                                externalDownRequestsToServe.remove(currentFloor);
                                currentCapacity++;
                                System.out.println("Elevator " + id + ": Picked up DOWN request at floor " + currentFloor);
                            } else {
                                System.out.println("Elevator " + id + ": Full, cannot pick up DOWN request at " + currentFloor);
                                // The ECS needs to be re-notified or handle this
                            }
                        }
                        // Important: After serving, notify the control system to clear the global pending request
                        // This is handled by the ElevatorControlSystem observing ElevatorArrival.

                        closeDoors();
                    }

                    // Determine if there are more requests in the current direction
                    boolean moreRequestsInCurrentDirection = false;
                    if (direction == Direction.UP) {
                        moreRequestsInCurrentDirection = internalRequests.stream().anyMatch(f -> f > currentFloor) ||
                                externalUpRequestsToServe.stream().anyMatch(f -> f > currentFloor);
                    } else if (direction == Direction.DOWN) {
                        moreRequestsInCurrentDirection = internalRequests.stream().anyMatch(f -> f < currentFloor) ||
                                externalDownRequestsToServe.stream().anyMatch(f -> f < currentFloor);
                    }

                    if (moreRequestsInCurrentDirection) {
                        move();
                    } else {
                        updateDirection(); // Re-evaluate direction if no more requests in current direction
                        if (direction != Direction.IDLE) { // If direction changed, move
                            move();
                        } else {
                            // If still idle, break and wait for signal again
                            // The outer loop's wait() will handle this
                        }
                    }
                }
            }
        });
    }

    public synchronized void resume() {
        this.notify(); // Wake up the elevator thread
    }

    public void shutdown() {
        elevatorExecutor.shutdownNow();
    }
}

class Floor {
    @Getter
    private final int floorNumber;
    private boolean hasUpRequest;
    private boolean hasDownRequest;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.hasUpRequest = false;
        this.hasDownRequest = false;
    }

    public synchronized void pressUpButton() {
        if (!hasUpRequest) {
            hasUpRequest = true;
            System.out.println("Floor " + floorNumber + ": UP button pressed.");
        }
    }

    public synchronized void pressDownButton() {
        if (!hasDownRequest) {
            hasDownRequest = true;
            System.out.println("Floor " + floorNumber + ": DOWN button pressed.");
        }
    }

    public synchronized void clearUpRequest() {
        if (hasUpRequest) {
            hasUpRequest = false;
            System.out.println("Floor " + floorNumber + ": UP request cleared.");
        }
    }

    public synchronized void clearDownRequest() {
        if (hasDownRequest) {
            hasDownRequest = false;
            System.out.println("Floor " + floorNumber + ": DOWN request cleared.");
        }
    }

    public synchronized boolean hasUpRequest() {
        return hasUpRequest;
    }

    public synchronized boolean hasDownRequest() {
        return hasDownRequest;
    }
}

class ElevatorControlSystem implements ElevatorObserver {
    @Getter
    private final List<Elevator> elevators;
    // Map of floor to a map of direction to a boolean (true if request pending)
    // This allows for concurrent UP/DOWN requests from the same floor
    private final Map<Integer, Map<Direction, Boolean>> pendingExternalRequests;
    private final List<ElevatorObserver> systemObservers; // Observers interested in system-wide events
    private final ExecutorService dispatcherExecutor;

    public ElevatorControlSystem() {
        this.elevators = new CopyOnWriteArrayList<>();
        this.pendingExternalRequests = new ConcurrentHashMap<>();
        this.systemObservers = new CopyOnWriteArrayList<>();
        this.dispatcherExecutor = Executors.newSingleThreadExecutor();
        startDispatcher();
    }

    public void addElevator(Elevator elevator) {
        elevators.add(elevator);
        elevator.addObserver(this); // Control system observes each elevator
    }

    public void addSystemObserver(ElevatorObserver observer) {
        systemObservers.add(observer);
    }

    public void requestElevator(int floor, Direction direction) {
        if (floor < Building.MIN_FLOOR || floor > Building.MAX_FLOOR) {
            System.out.println("Elevator Control System: Invalid floor request for floor " + floor);
            return;
        }

        pendingExternalRequests.computeIfAbsent(floor, k -> new ConcurrentHashMap<>()).put(direction, true);
        System.out.println("Elevator Control System: Received external request for floor " + floor + " in direction " + direction);
        dispatcherExecutor.submit(this::dispatchElevator); // Trigger dispatch
    }

    private void startDispatcher() {
        dispatcherExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Periodically check and dispatch, or triggered by new requests
                    Thread.sleep(500); // Poll every 500ms if no direct trigger
                    dispatchElevator();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }


    // Dispatch logic: Find the best elevator for pending requests
    private synchronized void dispatchElevator() {
        if (pendingExternalRequests.isEmpty()) {
            return;
        }

        // Iterate over a copy to avoid ConcurrentModificationException
        Set<Integer> floorsWithRequests = new HashSet<>(pendingExternalRequests.keySet());

        for (int floor : floorsWithRequests) {
            Map<Direction, Boolean> requestsAtFloor = pendingExternalRequests.get(floor);
            if (requestsAtFloor == null || requestsAtFloor.isEmpty()) {
                continue; // No active requests for this floor
            }

            // Prioritize UP then DOWN if both requested from same floor
            if (requestsAtFloor.containsKey(Direction.UP) && requestsAtFloor.get(Direction.UP)) {
                Elevator bestElevator = findBestElevator(floor, Direction.UP);
                if (bestElevator != null) {
                    bestElevator.assignExternalRequest(floor, Direction.UP);
                    bestElevator.resume(); // Wake up the elevator if it was idle
                    requestsAtFloor.put(Direction.UP, false); // Mark as assigned, will be cleared on arrival
                }
            }

            if (requestsAtFloor.containsKey(Direction.DOWN) && requestsAtFloor.get(Direction.DOWN)) {
                Elevator bestElevator = findBestElevator(floor, Direction.DOWN);
                if (bestElevator != null) {
                    bestElevator.assignExternalRequest(floor, Direction.DOWN);
                    bestElevator.resume(); // Wake up the elevator if it was idle
                    requestsAtFloor.put(Direction.DOWN, false); // Mark as assigned, will be cleared on arrival
                }
            }

            // Clean up floor if both directions are handled or no longer requested
            if (requestsAtFloor.values().stream().noneMatch(b -> b)) {
                pendingExternalRequests.remove(floor);
            }
        }
    }

    // Finds the best elevator based on direction and proximity
    private Elevator findBestElevator(int requestedFloor, Direction requestedDirection) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            if (elevator.isFull()) {
                continue; // Skip full elevators
            }

            int distance = Math.abs(elevator.getCurrentFloor() - requestedFloor);

            // 1. Same direction and already moving towards the floor
            if (elevator.getDirection() == requestedDirection) {
                if ((requestedDirection == Direction.UP && elevator.getCurrentFloor() <= requestedFloor) ||
                        (requestedDirection == Direction.DOWN && elevator.getCurrentFloor() >= requestedFloor)) {
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestElevator = elevator;
                    }
                }
            }
            // 2. Idle elevator
            else if (elevator.getDirection() == Direction.IDLE) {
                if (distance < minDistance) {
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }
            // 3. Elevator moving opposite direction, but will pass the floor on its way back
            // (Consider this less preferred than same direction or idle)
            else if (elevator.getDirection() != requestedDirection) {
                // If elevator is moving UP, and request is DOWN, and elevator is above requested floor
                // Or if elevator is moving DOWN, and request is UP, and elevator is below requested floor
                // This is a complex scenario, typically this elevator will serve requests in its current direction first
                // For simplicity, we prioritize same-direction or idle. If no such elevator, this one might be chosen.
                // For now, let's keep it simple and focus on current direction and idle.
                // A more advanced algorithm would calculate estimated time of arrival.
            }
        }
        return bestElevator;
    }

    @Override
    public void update(Elevator elevator) {
        // General update, can be used for logging or UI
        System.out.println("ECS Update: Elevator " + elevator.getId() + " at " + elevator.getCurrentFloor() +
                ", Dir: " + elevator.getDirection() + ", State: " + elevator.getState() +
                ", Internal Requests: " + elevator.getInternalRequests());
        // Notify system observers if needed
        for (ElevatorObserver observer : systemObservers) {
            observer.update(elevator);
        }
    }

    @Override
    public void onElevatorArrival(Elevator elevator, int floor) {
        // When an elevator arrives at a floor, clear the corresponding external request if it was served
        Map<Direction, Boolean> requestsAtFloor = pendingExternalRequests.get(floor);
        if (requestsAtFloor != null) {
            // Check if the elevator was assigned to serve an UP request at this floor
            if (elevator.getDirection() == Direction.UP && elevator.getExternalUpRequestsToServe().contains(floor)) {
                // This logic is tricky. The external request is removed from externalUpRequestsToServe inside Elevator.processRequests
                // when it's picked up. So here we just need to confirm it's cleared from the global pendingExternalRequests.
                // The `dispatchElevator` method marks it as false. If it becomes false and the other direction is also false, remove the floor.
                // A more robust way: Elevator notifies ECS which request it just served.
            }
            if (elevator.getDirection() == Direction.DOWN && elevator.getExternalDownRequestsToServe().contains(floor)) {
                // Same as above for DOWN
            }
            // If internal requests were dropped off at this floor
            // No direct impact on external requests unless the elevator was assigned to it.

            // Re-evaluate requests for this floor. If both directions are handled, remove the floor from pending.
            if (requestsAtFloor.containsKey(Direction.UP) && !elevator.getExternalUpRequestsToServe().contains(floor)) {
                requestsAtFloor.put(Direction.UP, false);
            }
            if (requestsAtFloor.containsKey(Direction.DOWN) && !elevator.getExternalDownRequestsToServe().contains(floor)) {
                requestsAtFloor.put(Direction.DOWN, false);
            }

            if (requestsAtFloor.values().stream().noneMatch(b -> b)) {
                pendingExternalRequests.remove(floor);
            }
        }
        // Notify system observers
        for (ElevatorObserver observer : systemObservers) {
            observer.onElevatorArrival(elevator, floor);
        }
    }

    @Override
    public void onElevatorDoorsOpen(Elevator elevator, int floor) {
        // Inform building to clear floor's button state
        Floor buildingFloor = Building.getInstance().getFloor(floor);
        if (buildingFloor != null) {
            if (elevator.getDirection() == Direction.UP) {
                buildingFloor.clearUpRequest();
            } else if (elevator.getDirection() == Direction.DOWN) {
                buildingFloor.clearDownRequest();
            }
        }
        for (ElevatorObserver observer : systemObservers) {
            observer.onElevatorDoorsOpen(elevator, floor);
        }
    }

    @Override
    public void onElevatorDoorsClose(Elevator elevator, int floor) {
        for (ElevatorObserver observer : systemObservers) {
            observer.onElevatorDoorsClose(elevator, floor);
        }
    }

    @Override
    public void onElevatorDirectionChange(Elevator elevator, Direction newDirection) {
        for (ElevatorObserver observer : systemObservers) {
            observer.onElevatorDirectionChange(elevator, newDirection);
        }
        // If elevator becomes IDLE and has no more internal/external requests assigned to it,
        // it means it's available for new assignments. This is implicitly handled by dispatchElevator.
    }

    public void shutdown() {
        dispatcherExecutor.shutdownNow();
        for (Elevator elevator : elevators) {
            elevator.shutdown();
        }
    }
}

// --- Factory Pattern ---

class ElevatorFactory {
    private static final AtomicInteger nextId = new AtomicInteger(1);

    public static Elevator createElevator(int maxCapacity) {
        return new Elevator(nextId.getAndIncrement(), maxCapacity);
    }
}

// --- Building (Singleton for simplicity, representing the entire system environment) ---

class Building implements ElevatorObserver { // Building can also observe elevator events
    private static Building instance;
    public static final int MIN_FLOOR = -2;
    public static final int MAX_FLOOR = 10;

    private final Map<Integer, Floor> floors;
    @Getter
    private final ElevatorControlSystem elevatorControlSystem;

    private Building() {
        this.floors = new ConcurrentHashMap<>();
        for (int i = MIN_FLOOR; i <= MAX_FLOOR; i++) {
            floors.put(i, new Floor(i));
        }
        this.elevatorControlSystem = new ElevatorControlSystem();
        this.elevatorControlSystem.addSystemObserver(this); // Building observes system-wide events
    }

    public static synchronized Building getInstance() {
        if (instance == null) {
            instance = new Building();
        }
        return instance;
    }

    public void setupBuilding(int numElevators, int elevatorCapacity) {
        for (int i = 0; i < numElevators; i++) {
            Elevator elevator = ElevatorFactory.createElevator(elevatorCapacity);
            elevatorControlSystem.addElevator(elevator);
        }
        System.out.println("Building setup complete with " + numElevators + " elevators.");
    }

    public Floor getFloor(int floorNumber) {
        return floors.get(floorNumber);
    }

    @Override
    public void update(Elevator elevator) {
        // Building specific update logic (e.g., updating UI, logging)
        // System.out.println("Building (Observer): Elevator " + elevator.getId() + " updated.");
    }

    @Override
    public void onElevatorArrival(Elevator elevator, int floor) {
        System.out.println("Building (Observer): Elevator " + elevator.getId() + " arrived at floor " + floor);
        // Here, the building could interact with the Floor object, e.g., clear its request button if an elevator arrives.
        // This is already handled by ElevatorControlSystem.onElevatorDoorsOpen.
    }

    @Override
    public void onElevatorDoorsOpen(Elevator elevator, int floor) {
        System.out.println("Building (Observer): Elevator " + elevator.getId() + " doors opened at floor " + floor);
    }

    @Override
    public void onElevatorDoorsClose(Elevator elevator, int floor) {
        System.out.println("Building (Observer): Elevator " + elevator.getId() + " doors closed at floor " + floor);
    }

    @Override
    public void onElevatorDirectionChange(Elevator elevator, Direction newDirection) {
        System.out.println("Building (Observer): Elevator " + elevator.getId() + " direction changed to " + newDirection);
    }

    public void shutdown() {
        elevatorControlSystem.shutdown();
    }
}


// --- Main Simulation ---

public class ElevatorSystemSimulation {
    public static void main(String[] args) throws InterruptedException {
        Building building = Building.getInstance();
        building.setupBuilding(2, 5); // 2 elevators, each with capacity 5

        ElevatorControlSystem ecs = building.getElevatorControlSystem();

        // Scenario 1: Basic UP request
        System.out.println("\n--- Scenario 1: Basic UP request ---");
        building.getFloor(2).pressUpButton();
        ecs.requestElevator(2, Direction.UP);
        Thread.sleep(500); // Give time for dispatcher to run

        Elevator elevator1 = ecs.getElevators().get(0);
        if (elevator1 != null) {
            elevator1.addInternalRequest(5); // Passenger inside elevator 1 requests floor 5
        }
        Thread.sleep(7000); // Wait for elevator to complete

        // Scenario 2: Basic DOWN request
        System.out.println("\n--- Scenario 2: Basic DOWN request ---");
        building.getFloor(8).pressDownButton();
        ecs.requestElevator(8, Direction.DOWN);
        Thread.sleep(500);

        Elevator elevator2 = ecs.getElevators().get(1);
        if (elevator2 != null) {
            elevator2.addInternalRequest(3); // Passenger inside elevator 2 requests floor 3
        }
        Thread.sleep(10000); // Wait

        // Scenario 3: Multiple requests, including negative floor
        System.out.println("\n--- Scenario 3: Multiple requests, including negative floor ---");
        building.getFloor(-1).pressUpButton();
        ecs.requestElevator(-1, Direction.UP);
        Thread.sleep(100);

        building.getFloor(7).pressDownButton();
        ecs.requestElevator(7, Direction.DOWN);
        Thread.sleep(100);

        building.getFloor(0).pressUpButton();
        ecs.requestElevator(0, Direction.UP);
        Thread.sleep(100);

        Thread.sleep(15000); // Allow time for all requests to be processed

        // Scenario 4: Conflicts from same floor (requesting both UP and DOWN)
        System.out.println("\n--- Scenario 4: Conflicts from same floor ---");
        building.getFloor(4).pressUpButton();
        ecs.requestElevator(4, Direction.UP);
        Thread.sleep(100);
        building.getFloor(4).pressDownButton();
        ecs.requestElevator(4, Direction.DOWN);
        Thread.sleep(100);

        // One elevator might pick up UP, another might pick up DOWN, or one might come for UP then return for DOWN
        Thread.sleep(15000);


        System.out.println("\n--- Simulation Complete ---");
        building.shutdown();
    }
}