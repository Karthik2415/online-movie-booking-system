package com.moviebooking.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class PaymentService {

    private static final String BOOKINGS_COLLECTION = "bookings";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    // ─── processPayment(bookingId, totalAmount) ────────────────

    public Map<String, Object> processPayment(String bookingId, double totalAmount)
            throws ExecutionException, InterruptedException {

        Firestore db = getFirestore();
        DocumentReference bookingRef = db.collection(BOOKINGS_COLLECTION).document(bookingId);

        // Verify booking exists
        if (!bookingRef.get().get().exists()) {
            throw new RuntimeException("Booking not found: " + bookingId);
        }

        // Simulate payment processing
        System.out.println("[PaymentService] Processing payment of ₹" + totalAmount + " for booking: " + bookingId);

        // In a real system, this would integrate with a payment gateway (Razorpay, Stripe, etc.)
        // For now, we simulate a successful payment.

        try {
            // Simulating payment gateway delay
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update payment status in Firestore
        bookingRef.update("paymentStatus", "paid").get();

        System.out.println("[PaymentService] Payment successful for booking: " + bookingId);

        Map<String, Object> paymentResult = new HashMap<>();
        paymentResult.put("bookingId", bookingId);
        paymentResult.put("totalAmount", totalAmount);
        paymentResult.put("paymentStatus", "paid");
        paymentResult.put("message", "Payment processed successfully");

        return paymentResult;
    }
}
