package BookMyShow;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

// --- Enums ---
enum SeatStatus {
    AVAILABLE,
    HELD,
    BOOKED,
    UNAVAILABLE, // For seats that are permanently blocked (e.g., maintenance)
    EXPIRED,
    CONVERTED_TO_BOOKED
}

enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    REFUNDED
}

enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}

enum SeatType {
    REGULAR,
    PREMIUM,
    RECLINER,
    ACCESSIBLE
}

enum EventType {
    MOVIE,
    CONCERT,
    PLAY,
    SPORTS
}

// --- Exceptions ---
class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}

class SeatHoldExpiredException extends RuntimeException {
    public SeatHoldExpiredException(String message) {
        super(message);
    }
}

class InvalidSeatHoldException extends RuntimeException {
    public InvalidSeatHoldException(String message) {
        super(message);
    }
}

class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}

// --- Entities (Simplified POJOs) ---

class User {
    private String userId;
    private String name;
    private String email;

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

class City {
    private String cityId;
    private String name;

    public City(String cityId, String name) {
        this.cityId = cityId;
        this.name = name;
    }

    public String getCityId() { return cityId; }
    public String getName() { return name; }
}

class Venue {
    private String venueId;
    private String name;
    private String address;
    private String cityId; // FK

    public Venue(String venueId, String name, String address, String cityId) {
        this.venueId = venueId;
        this.name = name;
        this.address = address;
        this.cityId = cityId;
    }

    public String getVenueId() { return venueId; }
    public String getName() { return name; }
    public String getCityId() { return cityId; }
}

class Auditorium {
    private String auditoriumId;
    private String name;
    private String venueId; // FK
    private int totalRows;
    private int totalColumns;
    // Map to store seat types for specific seats, or a default type
    private Map<String, SeatType> seatTypeMap; // "A1" -> REGULAR, "B2" -> PREMIUM

    public Auditorium(String auditoriumId, String name, String venueId, int totalRows, int totalColumns) {
        this.auditoriumId = auditoriumId;
        this.name = name;
        this.venueId = venueId;
        this.totalRows = totalRows;
        this.totalColumns = totalColumns;
        this.seatTypeMap = new HashMap<>();
        // Initialize all seats as REGULAR by default
        for (char rowChar = 'A'; rowChar < 'A' + totalRows; rowChar++) {
            for (int col = 1; col <= totalColumns; col++) {
                seatTypeMap.put(String.valueOf(rowChar) + col, SeatType.REGULAR);
            }
        }
    }

    public String getAuditoriumId() { return auditoriumId; }
    public String getName() { return name; }
    public String getVenueId() { return venueId; }
    public int getTotalRows() { return totalRows; }
    public int getTotalColumns() { return totalColumns; }
    public Map<String, SeatType> getSeatTypeMap() { return seatTypeMap; }

    public void setSeatType(String seatId, SeatType type) {
        if (seatTypeMap.containsKey(seatId)) {
            seatTypeMap.put(seatId, type);
        } else {
            System.err.println("Warning: Seat " + seatId + " not found in auditorium " + auditoriumId);
        }
    }
}

class Movie {
    private String movieId;
    private String title;
    private String genre;
    private int durationMinutes;
    private EventType type; // For flexibility, though named Movie

    public Movie(String movieId, String title, String genre, int durationMinutes) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.type = EventType.MOVIE; // Default for this example
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDurationMinutes() { return durationMinutes; }
    public EventType getType() { return type; }
}

class Show {
    private String showId;
    private String movieId; // FK
    private String auditoriumId; // FK
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double basePrice;
    private Map<SeatType, Double> seatTypePricing; // Additional pricing per seat type

    public Show(String showId, String movieId, String auditoriumId, LocalDateTime startTime, double basePrice) {
        this.showId = showId;
        this.movieId = movieId;
        this.auditoriumId = auditoriumId;
        this.startTime = startTime;
        // For simplicity, endTime is derived from movie duration later
        this.basePrice = basePrice;
        this.seatTypePricing = new HashMap<>();
        // Default pricing for seat types (can be overridden)
        seatTypePricing.put(SeatType.REGULAR, 0.0); // Base price covers regular
        seatTypePricing.put(SeatType.PREMIUM, 50.0);
        seatTypePricing.put(SeatType.RECLINER, 100.0);
        seatTypePricing.put(SeatType.ACCESSIBLE, 0.0); // Usually same as regular
    }

    public String getShowId() { return showId; }
    public String getMovieId() { return movieId; }
    public String getAuditoriumId() { return auditoriumId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; } // Will be set by service
    public double getBasePrice() { return basePrice; }
    public Map<SeatType, Double> getSeatTypePricing() { return seatTypePricing; }

    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setSeatTypePricing(Map<SeatType, Double> seatTypePricing) { this.seatTypePricing = seatTypePricing; }

    public double getPriceForSeatType(SeatType type) {
        return basePrice + seatTypePricing.getOrDefault(type, 0.0);
    }
}

// Seat state managed by BookingService, not a persistent entity itself in this simplified model
// For a real system, Seat would likely be a persistent entity with its own ID and status.
// Here, we represent a seat by its ID (e.g., "A1") and its current status for a specific show.

class SeatHold {
    private String seatHoldId;
    private String showId;
    private String userId;
    private List<String> seatIds;
    private LocalDateTime holdTimestamp;
    private LocalDateTime expiryTimestamp;
    private SeatStatus status; // HELD, EXPIRED, CONVERTED_TO_BOOKING

    public SeatHold(String seatHoldId, String showId, String userId, List<String> seatIds, long holdDurationMinutes) {
        this.seatHoldId = seatHoldId;
        this.showId = showId;
        this.userId = userId;
        this.seatIds = new ArrayList<>(seatIds);
        this.holdTimestamp = LocalDateTime.now();
        this.expiryTimestamp = holdTimestamp.plusMinutes(holdDurationMinutes);
        this.status = SeatStatus.HELD;
    }

    public String getSeatHoldId() { return seatHoldId; }
    public String getShowId() { return showId; }
    public String getUserId() { return userId; }
    public List<String> getSeatIds() { return seatIds; }
    public LocalDateTime getHoldTimestamp() { return holdTimestamp; }
    public LocalDateTime getExpiryTimestamp() { return expiryTimestamp; }
    public SeatStatus getStatus() { return status; }

    public void setStatus(SeatStatus status) { this.status = status; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTimestamp);
    }
}

class Booking {
    private String bookingId;
    private String showId;
    private String userId;
    private List<String> seatIds;
    private LocalDateTime bookingTimestamp;
    private double totalAmount;
    private String paymentId; // FK
    private BookingStatus status;
    private String confirmationCode; // Unique code for ticket

    public Booking(String bookingId, String showId, String userId, List<String> seatIds, double totalAmount, String paymentId, String confirmationCode) {
        this.bookingId = bookingId;
        this.showId = showId;
        this.userId = userId;
        this.seatIds = new ArrayList<>(seatIds);
        this.bookingTimestamp = LocalDateTime.now();
        this.totalAmount = totalAmount;
        this.paymentId = paymentId;
        this.status = BookingStatus.CONFIRMED; // Assumed confirmed after payment
        this.confirmationCode = confirmationCode;
    }

    public String getBookingId() { return bookingId; }
    public String getShowId() { return showId; }
    public String getUserId() { return userId; }
    public List<String> getSeatIds() { return seatIds; }
    public LocalDateTime getBookingTimestamp() { return bookingTimestamp; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentId() { return paymentId; }
    public BookingStatus getStatus() { return status; }
    public String getConfirmationCode() { return confirmationCode; }

    public void setStatus(BookingStatus status) { this.status = status; }
}

class Payment {
    private String paymentId;
    private String bookingId; // FK
    private String userId; // FK
    private double amount;
    private PaymentStatus status;
    private LocalDateTime paymentTimestamp;
    private String transactionRef; // Reference from external gateway

    public Payment(String paymentId, String bookingId, String userId, double amount, String transactionRef) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.userId = userId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING; // Initial status
        this.paymentTimestamp = LocalDateTime.now();
        this.transactionRef = transactionRef;
    }

    public String getPaymentId() { return paymentId; }
    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getPaymentTimestamp() { return paymentTimestamp; }
    public String getTransactionRef() { return transactionRef; }

    public void setStatus(PaymentStatus status) { this.status = status; }
}

// --- Services ---

// Component 1: User Management & Authentication Service (Simplified)
class UserManagementService {
    private Map<String, User> users = new ConcurrentHashMap<>();

    public User registerUser(String name, String email) {
        String userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User(userId, name, email);
        users.put(userId, user);
        System.out.println("User registered: " + user.getName() + " (ID: " + user.getUserId() + ")");
        return user;
    }

    public User getUser(String userId) {
        return users.get(userId);
    }
}

// Component 2: Location & Venue Management Service (Simplified)
class LocationVenueService {
    private Map<String, City> cities = new ConcurrentHashMap<>();
    private Map<String, Venue> venues = new ConcurrentHashMap<>();
    Map<String, Auditorium> auditoriums = new ConcurrentHashMap<>();

    public City addCity(String name) {
        String cityId = "city_" + UUID.randomUUID().toString().substring(0, 8);
        City city = new City(cityId, name);
        cities.put(cityId, city);
        return city;
    }

    public Venue addVenue(String name, String address, String cityId) {
        String venueId = "venue_" + UUID.randomUUID().toString().substring(0, 8);
        Venue venue = new Venue(venueId, name, address, cityId);
        venues.put(venueId, venue);
        return venue;
    }

    public Auditorium addAuditorium(String name, String venueId, int rows, int columns) {
        String auditoriumId = "audi_" + UUID.randomUUID().toString().substring(0, 8);
        Auditorium auditorium = new Auditorium(auditoriumId, name, venueId, rows, columns);
        auditoriums.put(auditoriumId, auditorium);
        return auditorium;
    }

    public Auditorium getAuditorium(String auditoriumId) {
        return auditoriums.get(auditoriumId);
    }

    public Venue getVenue(String venueId) {
        return venues.get(venueId);
    }

    public List<Venue> getVenuesInCity(String cityId) {
        return venues.values().stream()
                .filter(venue -> venue.getCityId().equals(cityId))
                .collect(Collectors.toList());
    }
}

// Component 3: Movie/Event Catalog Service (Simplified)
class MovieCatalogService {
    private Map<String, Movie> movies = new ConcurrentHashMap<>();

    public Movie addMovie(String title, String genre, int durationMinutes) {
        String movieId = "movie_" + UUID.randomUUID().toString().substring(0, 8);
        Movie movie = new Movie(movieId, title, genre, durationMinutes);
        movies.put(movieId, movie);
        return movie;
    }

    public Movie getMovie(String movieId) {
        return movies.get(movieId);
    }
}

// Component 4: Show Management Service (Simplified)
class ShowManagementService {
    Map<String, Show> shows = new ConcurrentHashMap<>();
    private MovieCatalogService movieCatalogService;
    private LocationVenueService locationVenueService;

    public ShowManagementService(MovieCatalogService movieCatalogService, LocationVenueService locationVenueService) {
        this.movieCatalogService = movieCatalogService;
        this.locationVenueService = locationVenueService;
    }

    public Show createShow(String movieId, String auditoriumId, LocalDateTime startTime, double basePrice) {
        Movie movie = movieCatalogService.getMovie(movieId);
        Auditorium auditorium = locationVenueService.getAuditorium(auditoriumId);

        if (movie == null || auditorium == null) {
            System.err.println("Error: Movie or Auditorium not found for show creation.");
            return null;
        }

        // Calculate end time based on movie duration
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());

        // Basic check for auditorium availability (no sophisticated conflict detection here)
        for (Show existingShow : shows.values()) {
            if (existingShow.getAuditoriumId().equals(auditoriumId) &&
                    !(endTime.isBefore(existingShow.getStartTime()) || startTime.isAfter(existingShow.getEndTime()))) {
                System.err.println("Error: Auditorium " + auditorium.getName() + " is already booked during this time slot.");
                return null;
            }
        }

        String showId = "show_" + UUID.randomUUID().toString().substring(0, 8);
        Show show = new Show(showId, movieId, auditoriumId, startTime, basePrice);
        show.setEndTime(endTime); // Set the calculated end time
        shows.put(showId, show);
        System.out.println("Show created: " + movie.getTitle() + " at " + auditorium.getName() + " (" + startTime + ")");
        return show;
    }

    public Show getShow(String showId) {
        return shows.get(showId);
    }

    public List<Show> getShowsForMovieInCity(String movieId, String cityId) {
        List<Venue> venuesInCity = locationVenueService.getVenuesInCity(cityId);
        Set<String> auditoriumIdsInCity = venuesInCity.stream()
                .flatMap(venue -> locationVenueService.auditoriums.values().stream()
                        .filter(a -> a.getVenueId().equals(venue.getVenueId())))
                .map(Auditorium::getAuditoriumId)
                .collect(Collectors.toSet());

        return shows.values().stream()
                .filter(show -> show.getMovieId().equals(movieId) && auditoriumIdsInCity.contains(show.getAuditoriumId()))
                .collect(Collectors.toList());
    }
}

// Component 6: Payment Service (Mocked External Gateway)
class PaymentService {
    private Map<String, Payment> payments = new ConcurrentHashMap<>();

    // Simulates calling an external payment gateway
    public Payment initiatePayment(String bookingId, String userId, double amount) {
        String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 8);
        String transactionRef = "txn_" + UUID.randomUUID().toString().substring(0, 8);
        Payment payment = new Payment(paymentId, bookingId, userId, amount, transactionRef);
        payments.put(paymentId, payment);
        System.out.println("Payment initiated for booking " + bookingId + ", Amount: " + amount + ", Payment ID: " + paymentId);
        return payment;
    }

    // Simulates a callback from an external payment gateway
    public boolean processPaymentCallback(String paymentId, PaymentStatus status) {
        Set<Integer> set = new HashSet<>();
        Payment payment = payments.get(paymentId);
        if (payment == null) {
            System.err.println("Error: Payment ID " + paymentId + " not found for callback.");
            return false;
        }
        payment.setStatus(status);
        System.out.println("Payment ID " + paymentId + " status updated to: " + status);
        return status == PaymentStatus.SUCCESS;
    }

    public Payment getPayment(String paymentId) {
        return payments.get(paymentId);
    }
}

// Component 5: Booking Service (Core Logic with Concurrency)
class BookingService {
    private Map<String, Show> shows; // Reference to all shows
    private Map<String, Auditorium> auditoriums; // Reference to all auditoriums
    private Map<String, SeatHold> seatHolds = new ConcurrentHashMap<>();
    private Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private PaymentService paymentService;

    // A map to hold locks for each show's seat management
    // This ensures that concurrent booking attempts for the SAME show are serialized
    private final ConcurrentHashMap<String, ReentrantLock> showLocks = new ConcurrentHashMap<>();

    // In-memory representation of seat status for each show
    // showId -> (seatId -> SeatStatus)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, SeatStatus>> showSeatStatuses = new ConcurrentHashMap<>();

    // For seat hold expiry cleanup
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    final long SEAT_HOLD_DURATION_MINUTES = 5; // How long a seat is held

    public BookingService(ShowManagementService showManagementService, LocationVenueService locationVenueService, PaymentService paymentService) {
        this.shows = showManagementService.shows; // Direct access for simplicity
        this.auditoriums = locationVenueService.auditoriums; // Direct access for simplicity
        this.paymentService = paymentService;

        // Initialize seat statuses for existing shows
        shows.values().forEach(this::initializeShowSeats);

        // Schedule a task to clean up expired seat holds
        scheduler.scheduleAtFixedRate(this::releaseExpiredHolds, 1, 1, TimeUnit.MINUTES);
    }

    private void initializeShowSeats(Show show) {
        Auditorium auditorium = auditoriums.get(show.getAuditoriumId());
        if (auditorium != null) {
            ConcurrentHashMap<String, SeatStatus> seatsForShow = new ConcurrentHashMap<>();
            for (char rowChar = 'A'; rowChar < 'A' + auditorium.getTotalRows(); rowChar++) {
                for (int col = 1; col <= auditorium.getTotalColumns(); col++) {
                    seatsForShow.put(String.valueOf(rowChar) + col, SeatStatus.AVAILABLE);
                }
            }
            showSeatStatuses.put(show.getShowId(), seatsForShow);
        }
    }

    // Design Principle: Concurrency Control (using ReentrantLock per show)
    // This method handles selecting and holding seats, preventing race conditions for the same show.
    public SeatHold reserveSeats(String showId, String userId, List<String> requestedSeatIds) {
        // Get or create a lock for this specific show
        ReentrantLock showLock = showLocks.computeIfAbsent(showId, k -> new ReentrantLock());
        showLock.lock(); // Acquire the lock for this show

        try {
            ConcurrentHashMap<String, SeatStatus> seatsForShow = showSeatStatuses.get(showId);
            Show show = shows.get(showId);
            Auditorium auditorium = auditoriums.get(show.getAuditoriumId());

            if (seatsForShow == null || show == null || auditorium == null) {
                throw new IllegalArgumentException("Invalid show or auditorium ID.");
            }

            // 1. Validate requested seats exist and are available
            for (String seatId : requestedSeatIds) {
                if (!auditorium.getSeatTypeMap().containsKey(seatId)) {
                    throw new SeatUnavailableException("Seat " + seatId + " does not exist in auditorium " + auditorium.getName());
                }
                SeatStatus currentStatus = seatsForShow.get(seatId);
                if (currentStatus != SeatStatus.AVAILABLE) {
                    throw new SeatUnavailableException("Seat " + seatId + " is " + currentStatus + " for show " + show.getShowId());
                }
            }

            // 2. Mark seats as HELD
            for (String seatId : requestedSeatIds) {
                seatsForShow.put(seatId, SeatStatus.HELD);
            }

            // 3. Create SeatHold object
            String seatHoldId = "hold_" + UUID.randomUUID().toString().substring(0, 8);
            SeatHold seatHold = new SeatHold(seatHoldId, showId, userId, requestedSeatIds, SEAT_HOLD_DURATION_MINUTES);
            seatHolds.put(seatHoldId, seatHold);

            System.out.println("Seats " + requestedSeatIds + " held for user " + userId + " on show " + showId + " (Hold ID: " + seatHoldId + ")");
            return seatHold;

        } finally {
            showLock.unlock(); // Release the lock
        }
    }

    // Design Principle: Atomicity (conceptual transaction)
    // This method confirms a booking after payment, ensuring all related state changes happen together.
    public Booking confirmBooking(String seatHoldId, String userId) {
        SeatHold seatHold = seatHolds.get(seatHoldId);

        if (seatHold == null || !seatHold.getUserId().equals(userId) || seatHold.getStatus() != SeatStatus.HELD) {
            throw new InvalidSeatHoldException("Invalid or expired seat hold ID: " + seatHoldId);
        }
        if (seatHold.isExpired()) {
            releaseSeats(seatHold.getShowId(), seatHold.getSeatIds()); // Release the expired hold
            seatHold.setStatus(SeatStatus.EXPIRED);
            throw new SeatHoldExpiredException("Seat hold " + seatHoldId + " has expired.");
        }

        // Calculate total amount
        Show show = shows.get(seatHold.getShowId());
        Auditorium auditorium = auditoriums.get(show.getAuditoriumId());
        double totalAmount = 0.0;
        for (String seatId : seatHold.getSeatIds()) {
            SeatType seatType = auditorium.getSeatTypeMap().getOrDefault(seatId, SeatType.REGULAR);
            totalAmount += show.getPriceForSeatType(seatType);
        }

        // --- Simulate Payment Process ---
        // In a real system, this would involve calling a payment gateway,
        // waiting for a callback, and then proceeding.
        Payment payment = paymentService.initiatePayment(seatHoldId, userId, totalAmount);
        boolean paymentSuccess = paymentService.processPaymentCallback(payment.getPaymentId(), PaymentStatus.SUCCESS); // Mock success

        if (!paymentSuccess) {
            // If payment fails, release the hold
            releaseSeats(seatHold.getShowId(), seatHold.getSeatIds());
            seatHold.setStatus(SeatStatus.EXPIRED); // Mark hold as expired due to payment failure
            throw new PaymentFailedException("Payment failed for seat hold " + seatHoldId);
        }

        // --- Critical Section: Convert Hold to Booking ---
        ReentrantLock showLock = showLocks.get(seatHold.getShowId());
        if (showLock == null) { // Should not happen if reserveSeats was called
            throw new IllegalStateException("No lock found for show: " + seatHold.getShowId());
        }
        showLock.lock(); // Acquire lock for the show to update seat statuses

        try {
            ConcurrentHashMap<String, SeatStatus> seatsForShow = showSeatStatuses.get(seatHold.getShowId());

            // Double-check seat status (optimistic check, though lock helps here)
            for (String seatId : seatHold.getSeatIds()) {
                if (seatsForShow.get(seatId) != SeatStatus.HELD) {
                    // This scenario means something went wrong, possibly another release process.
                    // In a real system, this would trigger a more complex reconciliation or error.
                    System.err.println("Warning: Seat " + seatId + " not in HELD state during booking confirmation. Actual: " + seatsForShow.get(seatId));
                    // Attempt to rollback or handle gracefully. For now, throw.
                    throw new SeatUnavailableException("One or more seats are no longer held.");
                }
            }

            // Mark seats as BOOKED
            for (String seatId : seatHold.getSeatIds()) {
                seatsForShow.put(seatId, SeatStatus.BOOKED);
            }

            // Create Booking object
            String bookingId = "book_" + UUID.randomUUID().toString().substring(0, 8);
            String confirmationCode = generateConfirmationCode();
            Booking booking = new Booking(bookingId, seatHold.getShowId(), userId, seatHold.getSeatIds(), totalAmount, payment.getPaymentId(), confirmationCode);
            bookings.put(bookingId, booking);

            // Update SeatHold status
            seatHold.setStatus(SeatStatus.CONVERTED_TO_BOOKED);

            System.out.println("Booking confirmed for user " + userId + " on show " + seatHold.getShowId() + ". Booking ID: " + bookingId + ", Confirmation Code: " + confirmationCode);
            return booking;

        } finally {
            showLock.unlock(); // Release the lock
        }
    }

    // Releases seats from HELD status back to AVAILABLE
    private void releaseSeats(String showId, List<String> seatIds) {
        ReentrantLock showLock = showLocks.get(showId);
        if (showLock == null) return; // Should not happen

        showLock.lock();
        try {
            ConcurrentHashMap<String, SeatStatus> seatsForShow = showSeatStatuses.get(showId);
            if (seatsForShow != null) {
                for (String seatId : seatIds) {
                    // Only release if it's HELD or if it's already EXPIRED (defensive)
                    if (seatsForShow.get(seatId) == SeatStatus.HELD || seatsForShow.get(seatId) == SeatStatus.EXPIRED) {
                        seatsForShow.put(seatId, SeatStatus.AVAILABLE);
                    }
                }
            }
            System.out.println("Seats " + seatIds + " released for show " + showId);
        } finally {
            showLock.unlock();
        }
    }

    // Scheduled task to release expired seat holds
    // Design Principle: Fault Tolerance (handling abandoned flows)
    private void releaseExpiredHolds() {
        System.out.println("\n--- Running expired seat hold cleanup ---");
        LocalDateTime now = LocalDateTime.now();
        seatHolds.values().stream()
                .filter(hold -> hold.getStatus() == SeatStatus.HELD && hold.isExpired())
                .forEach(hold -> {
                    System.out.println("Releasing expired hold: " + hold.getSeatHoldId() + " for show " + hold.getShowId());
                    releaseSeats(hold.getShowId(), hold.getSeatIds());
                    hold.setStatus(SeatStatus.EXPIRED); // Mark the hold itself as expired
                });
        System.out.println("--- Expired seat hold cleanup complete ---\n");
    }

    // Get current seat availability for a show
    public Map<String, SeatStatus> getAvailableSeats(String showId) {
        ConcurrentHashMap<String, SeatStatus> seatsForShow = showSeatStatuses.get(showId);
        if (seatsForShow == null) {
            return Collections.emptyMap();
        }
        // Return a copy to prevent external modification
        return new HashMap<>(seatsForShow);
    }

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }

    private String generateConfirmationCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // Shutdown the scheduler when the application closes
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

// --- Main Application Class for Demonstration ---
public class BookMyShowApp {
    public static void main(String[] args) throws InterruptedException {
        // Initialize Services
        UserManagementService userService = new UserManagementService();
        LocationVenueService locationVenueService = new LocationVenueService();
        MovieCatalogService movieCatalogService = new MovieCatalogService();
        ShowManagementService showManagementService = new ShowManagementService(movieCatalogService, locationVenueService);
        PaymentService paymentService = new PaymentService();
        BookingService bookingService = new BookingService(showManagementService, locationVenueService, paymentService);

        // --- Setup Data ---
        User user1 = userService.registerUser("Alice", "alice@example.com");
        User user2 = userService.registerUser("Bob", "bob@example.com");

        City bangalore = locationVenueService.addCity("Bangalore");
        Venue pvrForum = locationVenueService.addVenue("PVR Forum", "Koramangala, Bangalore", bangalore.getCityId());
        Auditorium audi1 = locationVenueService.addAuditorium("Audi 1", pvrForum.getVenueId(), 5, 10); // 5 rows, 10 cols
        Auditorium audi2 = locationVenueService.addAuditorium("Audi 2 (IMAX)", pvrForum.getVenueId(), 8, 15);

        // Customize seat types for Audi 1
        audi1.setSeatType("A1", SeatType.ACCESSIBLE);
        audi1.setSeatType("A2", SeatType.ACCESSIBLE);
        audi1.setSeatType("E5", SeatType.PREMIUM);
        audi1.setSeatType("E6", SeatType.PREMIUM);

        Movie movie1 = movieCatalogService.addMovie("Dune: Part Two", "Sci-Fi", 166);
        Movie movie2 = movieCatalogService.addMovie("Kung Fu Panda 4", "Animation", 94);

        Show show1 = showManagementService.createShow(movie1.getMovieId(), audi1.getAuditoriumId(), LocalDateTime.now().plusHours(2), 200.0);
        Show show2 = showManagementService.createShow(movie2.getMovieId(), audi2.getAuditoriumId(), LocalDateTime.now().plusHours(3), 250.0);

        // --- Demonstration of Booking Flow ---
        System.out.println("\n--- User 1 (Alice) tries to book seats ---");
        List<String> aliceSeats = Arrays.asList("C1", "C2", "C3");
        SeatHold aliceHold = null;
        try {
            aliceHold = bookingService.reserveSeats(show1.getShowId(), user1.getUserId(), aliceSeats);
            System.out.println("Alice's hold ID: " + aliceHold.getSeatHoldId());
        } catch (SeatUnavailableException e) {
            System.err.println("Alice failed to hold seats: " + e.getMessage());
        }

        // Check seat status after Alice holds
        System.out.println("\n--- Current seat status for Show 1 after Alice's hold ---");
        bookingService.getAvailableSeats(show1.getShowId()).entrySet().stream()
                .filter(entry -> aliceSeats.contains(entry.getKey()))
                .forEach(entry -> System.out.println("Seat " + entry.getKey() + ": " + entry.getValue()));

        System.out.println("\n--- User 2 (Bob) tries to book one of Alice's held seats ---");
        List<String> bobSeatsAttempt = Arrays.asList("C2", "D5");
        SeatHold bobHold = null;
        try {
            bobHold = bookingService.reserveSeats(show1.getShowId(), user2.getUserId(), bobSeatsAttempt);
            System.out.println("Bob's hold ID: " + bobHold.getSeatHoldId());
        } catch (SeatUnavailableException e) {
            System.err.println("Bob failed to hold seats (expected for C2): " + e.getMessage());
        }

        // --- Confirm Alice's Booking ---
        if (aliceHold != null) {
            System.out.println("\n--- Alice proceeds to confirm her booking ---");
            try {
                Booking aliceBooking = bookingService.confirmBooking(aliceHold.getSeatHoldId(), user1.getUserId());
                System.out.println("Alice's booking confirmed! Confirmation Code: " + aliceBooking.getConfirmationCode());
            } catch (Exception e) {
                System.err.println("Alice's booking failed: " + e.getMessage());
            }
        }

        // Check seat status after Alice's booking
        System.out.println("\n--- Current seat status for Show 1 after Alice's booking ---");
        bookingService.getAvailableSeats(show1.getShowId()).entrySet().stream()
                .filter(entry -> aliceSeats.contains(entry.getKey()))
                .forEach(entry -> System.out.println("Seat " + entry.getKey() + ": " + entry.getValue()));

        // --- Demonstrate Seat Hold Expiry ---
        System.out.println("\n--- Demonstrating Seat Hold Expiry ---");
        List<String> tempSeats = Arrays.asList("E1", "E2");
        SeatHold tempHold = bookingService.reserveSeats(show1.getShowId(), user1.getUserId(), tempSeats);
        System.out.println("Temporary hold created: " + tempHold.getSeatHoldId() + ". Will expire in " + bookingService.SEAT_HOLD_DURATION_MINUTES + " minutes.");

        System.out.println("Waiting for " + (bookingService.SEAT_HOLD_DURATION_MINUTES + 1) + " minutes to trigger expiry cleanup...");
        Thread.sleep((bookingService.SEAT_HOLD_DURATION_MINUTES + 1) * 60 * 1000); // Wait for hold to expire + 1 min for scheduler

        // After waiting, the scheduled task should have released the seats.
        System.out.println("\n--- Seat status for temporary seats after expiry ---");
        bookingService.getAvailableSeats(show1.getShowId()).entrySet().stream()
                .filter(entry -> tempSeats.contains(entry.getKey()))
                .forEach(entry -> System.out.println("Seat " + entry.getKey() + ": " + entry.getValue()));


        // --- Attempt to book expired hold ---
        System.out.println("\n--- Attempting to confirm expired hold ---");
        try {
            bookingService.confirmBooking(tempHold.getSeatHoldId(), user1.getUserId());
        } catch (SeatHoldExpiredException e) {
            System.err.println("As expected: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }

        // Shutdown the scheduler
        bookingService.shutdown();
    }
}
