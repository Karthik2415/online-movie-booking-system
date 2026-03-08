package com.moviebooking.controller;

import com.moviebooking.model.Booking;
import com.moviebooking.service.BookingService;
import com.moviebooking.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    // ─── POST /api/booking ─────────────────────────────────────
    // Create a booking → process payment → confirm → return ticket

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String showId = (String) request.get("showId");

            @SuppressWarnings("unchecked")
            List<String> seatNumbers = (List<String>) request.get("seatNumbers");

            // 1. Create booking
            Booking booking = bookingService.createBooking(userId, showId, seatNumbers);

            // 2. Process payment
            paymentService.processPayment(booking.getBookingId(), booking.getTotalAmount());

            // 3. Confirm booking (updates shows collection)
            bookingService.confirmBooking(booking.getBookingId());

            // 4. Generate ticket
            Map<String, Object> ticket = bookingService.generateTicket(booking.getBookingId());

            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Booking failed: " + e.getMessage()));
        }
    }

    // ─── GET /api/booking/{userId} ─────────────────────────────
    // Fetch all bookings for a user

    @GetMapping("/{userId}")
    public ResponseEntity<?> getBookings(@PathVariable String userId) {
        try {
            List<Booking> bookings = bookingService.getBookingsByUser(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch bookings: " + e.getMessage()));
        }
    }

    // ─── DELETE /api/booking/{bookingId} ───────────────────────
    // Cancel a booking

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable String bookingId) {
        try {
            Booking cancelled = bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok(Map.of(
                    "message", "Booking cancelled successfully",
                    "bookingId", cancelled.getBookingId(),
                    "bookingStatus", cancelled.getBookingStatus(),
                    "paymentStatus", cancelled.getPaymentStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Cancellation failed: " + e.getMessage()));
        }
    }

    // ─── GET /api/booking/seats/{showId} ───────────────────────
    // Get seat availability for a show

    @GetMapping("/seats/{showId}")
    public ResponseEntity<?> getSeats(@PathVariable String showId) {
        try {
            Map<String, Object> seatInfo = bookingService.selectSeats(showId);
            return ResponseEntity.ok(seatInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch seats: " + e.getMessage()));
        }
    }
}
