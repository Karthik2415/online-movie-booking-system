package com.moviebooking.firebase;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            // Check if data already exists
            DocumentSnapshot existing = db.collection("movies").document("movie001").get().get();
            if (existing.exists()) {
                System.out.println("[DataSeeder] Sample data already exists. Skipping.");
                return;
            }

            System.out.println("[DataSeeder] Seeding sample movies and shows...");

            // ─── Movies ────────────────────────────────────────
            Map<String, Object> movie1 = new HashMap<>();
            movie1.put("movieId", "movie001");
            movie1.put("movieName", "Avengers: Endgame");
            movie1.put("genre", "Action");
            movie1.put("duration", "181 mins");
            movie1.put("language", "English");
            db.collection("movies").document("movie001").set(movie1).get();

            Map<String, Object> movie2 = new HashMap<>();
            movie2.put("movieId", "movie002");
            movie2.put("movieName", "RRR");
            movie2.put("genre", "Action/Drama");
            movie2.put("duration", "187 mins");
            movie2.put("language", "Telugu");
            db.collection("movies").document("movie002").set(movie2).get();

            Map<String, Object> movie3 = new HashMap<>();
            movie3.put("movieId", "movie003");
            movie3.put("movieName", "Inception");
            movie3.put("genre", "Sci-Fi/Thriller");
            movie3.put("duration", "148 mins");
            movie3.put("language", "English");
            db.collection("movies").document("movie003").set(movie3).get();

            // ─── Shows ─────────────────────────────────────────
            Map<String, Object> show1 = new HashMap<>();
            show1.put("showId", "show001");
            show1.put("movieId", "movie001");
            show1.put("theaterId", "theater01");
            show1.put("showTime", "10:00 AM");
            show1.put("availableSeats", 15);
            show1.put("bookedSeats", new ArrayList<String>());
            db.collection("shows").document("show001").set(show1).get();

            Map<String, Object> show2 = new HashMap<>();
            show2.put("showId", "show002");
            show2.put("movieId", "movie001");
            show2.put("theaterId", "theater01");
            show2.put("showTime", "02:00 PM");
            show2.put("availableSeats", 15);
            show2.put("bookedSeats", new ArrayList<String>());
            db.collection("shows").document("show002").set(show2).get();

            Map<String, Object> show3 = new HashMap<>();
            show3.put("showId", "show003");
            show3.put("movieId", "movie002");
            show3.put("theaterId", "theater02");
            show3.put("showTime", "06:00 PM");
            show3.put("availableSeats", 15);
            show3.put("bookedSeats", new ArrayList<String>());
            db.collection("shows").document("show003").set(show3).get();

            Map<String, Object> show4 = new HashMap<>();
            show4.put("showId", "show004");
            show4.put("movieId", "movie003");
            show4.put("theaterId", "theater01");
            show4.put("showTime", "09:00 PM");
            show4.put("availableSeats", 15);
            show4.put("bookedSeats", new ArrayList<String>());
            db.collection("shows").document("show004").set(show4).get();

            System.out.println("[DataSeeder] ✅ Sample data seeded successfully!");
            System.out.println("[DataSeeder] Available shows: show001, show002, show003, show004");

        } catch (Exception e) {
            System.err.println("[DataSeeder] WARNING: Could not seed data: " + e.getMessage());
        }
    }
}
