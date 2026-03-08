package com.moviebooking.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.moviebooking.model.Booking;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class BookingService {

    private static final String BOOKINGS_COLLECTION = "bookings";
    private static final String SHOWS_COLLECTION = "shows";
    private static final String MOVIES_COLLECTION = "movies";
    private static final double PRICE_PER_SEAT = 150.0;

    // All possible seats in the theater grid
    private static final String[][] SEAT_GRID = {
            {"A1", "A2", "A3", "A4", "A5"},
            {"B1", "B2", "B3", "B4", "B5"},
            {"C1", "C2", "C3", "C4", "C5"}
    };

    // ─── Get Firestore instance ────────────────────────────────

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    // ─── selectSeats(showId) ───────────────────────────────────
    // Returns seat availability map for a given show

    @SuppressWarnings("unchecked")
    public Map<String, Object> selectSeats(String showId) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentSnapshot showDoc = db.collection(SHOWS_COLLECTION).document(showId).get().get();

        if (!showDoc.exists()) {
            throw new RuntimeException("Show not found: " + showId);
        }

        // Seats already booked for this show
        List<String> bookedSeats = new ArrayList<>();
        Object bookedField = showDoc.get("bookedSeats");
        if (bookedField instanceof List) {
            bookedSeats = (List<String>) bookedField;
        }

        int availableSeats = showDoc.getLong("availableSeats").intValue();

        // Build seat map: seat -> "available" / "booked"
        Map<String, String> seatMap = new LinkedHashMap<>();
        for (String[] row : SEAT_GRID) {
            for (String seat : row) {
                seatMap.put(seat, bookedSeats.contains(seat) ? "booked" : "available");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("showId", showId);
        result.put("availableSeats", availableSeats);
        result.put("seatGrid", SEAT_GRID);
        result.put("seatStatus", seatMap);
        return result;
    }

    // ─── calculateTotal(seatNumbers) ───────────────────────────

    public double calculateTotal(List<String> seatNumbers) {
        return seatNumbers.size() * PRICE_PER_SEAT;
    }

    // ─── createBooking(userId, showId, seatNumbers) ────────────

    public Booking createBooking(String userId, String showId, List<String> seatNumbers)
            throws ExecutionException, InterruptedException {

        Firestore db = getFirestore();

        // Validate show exists
        DocumentSnapshot showDoc = db.collection(SHOWS_COLLECTION).document(showId).get().get();
        if (!showDoc.exists()) {
            throw new RuntimeException("Show not found: " + showId);
        }

        // Check seat availability
        int availableSeats = showDoc.getLong("availableSeats").intValue();
        if (seatNumbers.size() > availableSeats) {
            throw new RuntimeException("Not enough seats available. Requested: "
                    + seatNumbers.size() + ", Available: " + availableSeats);
        }

        // Check if any selected seat is already booked
        @SuppressWarnings("unchecked")
        List<String> bookedSeats = showDoc.get("bookedSeats") instanceof List
                ? (List<String>) showDoc.get("bookedSeats")
                : new ArrayList<>();

        for (String seat : seatNumbers) {
            if (bookedSeats.contains(seat)) {
                throw new RuntimeException("Seat " + seat + " is already booked!");
            }
        }

        // Generate booking
        String bookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double totalAmount = calculateTotal(seatNumbers);
        String bookingDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Booking booking = new Booking(
                bookingId, userId, showId,
                seatNumbers, totalAmount,
                "pending",    // paymentStatus
                "pending",    // bookingStatus
                bookingDate
        );

        // Save to Firestore bookings collection
        db.collection(BOOKINGS_COLLECTION).document(bookingId).set(booking).get();

        System.out.println("[BookingService] Booking created: " + bookingId);
        return booking;
    }

    // ─── confirmBooking(bookingId) ─────────────────────────────

    @SuppressWarnings("unchecked")
    public Booking confirmBooking(String bookingId) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference bookingRef = db.collection(BOOKINGS_COLLECTION).document(bookingId);
        DocumentSnapshot bookingDoc = bookingRef.get().get();

        if (!bookingDoc.exists()) {
            throw new RuntimeException("Booking not found: " + bookingId);
        }

        // Update booking status
        bookingRef.update("bookingStatus", "confirmed").get();

        // Reduce availableSeats and add to bookedSeats in shows collection
        String showId = bookingDoc.getString("showId");
        List<String> seatNumbers = (List<String>) bookingDoc.get("seatNumbers");

        DocumentReference showRef = db.collection(SHOWS_COLLECTION).document(showId);
        DocumentSnapshot showDoc = showRef.get().get();

        int currentAvailable = showDoc.getLong("availableSeats").intValue();
        List<String> currentBooked = showDoc.get("bookedSeats") instanceof List
                ? new ArrayList<>((List<String>) showDoc.get("bookedSeats"))
                : new ArrayList<>();

        currentBooked.addAll(seatNumbers);

        showRef.update(
                "availableSeats", currentAvailable - seatNumbers.size(),
                "bookedSeats", currentBooked
        ).get();

        System.out.println("[BookingService] Booking confirmed: " + bookingId);

        // Return updated booking
        Booking confirmed = bookingDoc.toObject(Booking.class);
        confirmed.setBookingStatus("confirmed");
        return confirmed;
    }

    // ─── cancelBooking(bookingId) ──────────────────────────────

    @SuppressWarnings("unchecked")
    public Booking cancelBooking(String bookingId) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference bookingRef = db.collection(BOOKINGS_COLLECTION).document(bookingId);
        DocumentSnapshot bookingDoc = bookingRef.get().get();

        if (!bookingDoc.exists()) {
            throw new RuntimeException("Booking not found: " + bookingId);
        }

        String currentStatus = bookingDoc.getString("bookingStatus");
        if ("cancelled".equals(currentStatus)) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        // Update booking status
        bookingRef.update("bookingStatus", "cancelled", "paymentStatus", "refunded").get();

        // Restore seats if booking was confirmed
        if ("confirmed".equals(currentStatus)) {
            String showId = bookingDoc.getString("showId");
            List<String> seatNumbers = (List<String>) bookingDoc.get("seatNumbers");

            DocumentReference showRef = db.collection(SHOWS_COLLECTION).document(showId);
            DocumentSnapshot showDoc = showRef.get().get();

            int currentAvailable = showDoc.getLong("availableSeats").intValue();
            List<String> currentBooked = showDoc.get("bookedSeats") instanceof List
                    ? new ArrayList<>((List<String>) showDoc.get("bookedSeats"))
                    : new ArrayList<>();

            currentBooked.removeAll(seatNumbers);

            showRef.update(
                    "availableSeats", currentAvailable + seatNumbers.size(),
                    "bookedSeats", currentBooked
            ).get();
        }

        System.out.println("[BookingService] Booking cancelled: " + bookingId);

        Booking cancelled = bookingDoc.toObject(Booking.class);
        cancelled.setBookingStatus("cancelled");
        cancelled.setPaymentStatus("refunded");
        return cancelled;
    }

    // ─── generateTicket(bookingId) ─────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> generateTicket(String bookingId)
            throws ExecutionException, InterruptedException {

        Firestore db = getFirestore();
        DocumentSnapshot bookingDoc = db.collection(BOOKINGS_COLLECTION).document(bookingId).get().get();

        if (!bookingDoc.exists()) {
            throw new RuntimeException("Booking not found: " + bookingId);
        }

        String showId = bookingDoc.getString("showId");
        DocumentSnapshot showDoc = db.collection(SHOWS_COLLECTION).document(showId).get().get();

        String movieId = showDoc.getString("movieId");
        DocumentSnapshot movieDoc = db.collection(MOVIES_COLLECTION).document(movieId).get().get();

        // Build ticket summary
        Map<String, Object> ticket = new LinkedHashMap<>();
        ticket.put("bookingId", bookingId);
        ticket.put("movieName", movieDoc.getString("movieName"));
        ticket.put("showTime", showDoc.getString("showTime"));
        ticket.put("theaterId", showDoc.getString("theaterId"));
        ticket.put("seatNumbers", bookingDoc.get("seatNumbers"));
        ticket.put("totalAmount", bookingDoc.getDouble("totalAmount"));
        ticket.put("paymentStatus", bookingDoc.getString("paymentStatus"));
        ticket.put("bookingStatus", bookingDoc.getString("bookingStatus"));
        ticket.put("bookingDate", bookingDoc.getString("bookingDate"));

        System.out.println("[BookingService] Ticket generated for: " + bookingId);
        return ticket;
    }

    // ─── getBookingsByUser(userId) ─────────────────────────────

    public List<Booking> getBookingsByUser(String userId) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Booking> bookings = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            bookings.add(doc.toObject(Booking.class));
        }
        return bookings;
    }
}
