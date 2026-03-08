package com.moviebooking.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseInitializer {

    @PostConstruct
    public void initialize() {
        try {
            // Try loading from classpath first
            InputStream serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount == null) {
                // Fallback: load from project root
                serviceAccount = new FileInputStream("serviceAccountKey.json");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("[Firebase] Initialized successfully.");
            }

        } catch (IOException e) {
            System.err.println("[Firebase] WARNING: Could not initialize Firebase.");
            System.err.println("  Place your serviceAccountKey.json in src/main/resources/");
            System.err.println("  Error: " + e.getMessage());
        }
    }
}
