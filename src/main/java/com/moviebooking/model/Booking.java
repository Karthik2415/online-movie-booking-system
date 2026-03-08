package com.moviebooking.model;

import java.util.List;

public class Booking {

    private String bookingId;
    private String userId;
    private String showId;
    private List<String> seatNumbers;
    private double totalAmount;
    private String paymentStatus;   // "pending", "paid", "refunded"
    private String bookingStatus;   // "pending", "confirmed", "cancelled"
    private String bookingDate;

    // Default constructor (required for Firestore deserialization)
    public Booking() {
    }

    // Parameterized constructor
    public Booking(String bookingId, String userId, String showId,
                   List<String> seatNumbers, double totalAmount,
                   String paymentStatus, String bookingStatus, String bookingDate) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.showId = showId;
        this.seatNumbers = seatNumbers;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.bookingStatus = bookingStatus;
        this.bookingDate = bookingDate;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", userId='" + userId + '\'' +
                ", showId='" + showId + '\'' +
                ", seatNumbers=" + seatNumbers +
                ", totalAmount=" + totalAmount +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", bookingStatus='" + bookingStatus + '\'' +
                ", bookingDate='" + bookingDate + '\'' +
                '}';
    }
}
