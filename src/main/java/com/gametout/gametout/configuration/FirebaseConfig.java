package com.gametout.gametout.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import java.io.InputStream;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
// import org.springframework.core.io.ClassPathResource; 
import java.io.FileInputStream;


@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return;

        String firebasePath = System.getenv("FIREBASE_CREDENTIALS");

        if (firebasePath == null) {
            throw new IllegalStateException("FIREBASE_CREDENTIALS env var not set");
        }

        try (InputStream serviceAccount = new FileInputStream(firebasePath)) {

            GoogleCredentials credentials =
                    GoogleCredentials.fromStream(serviceAccount);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }
}


// @Configuration
// public class FirebaseConfig {

//     @PostConstruct
//     public void init() throws IOException {
//         if (!FirebaseApp.getApps().isEmpty()) return;

//         InputStream serviceAccount = new ClassPathResource("fir-8822d-firebase-adminsdk-fbsvc-6ad56cbd92.json").getInputStream();

//         GoogleCredentials credentials =
//             GoogleCredentials.fromStream(serviceAccount);

//         FirebaseOptions options = FirebaseOptions.builder()
//                 .setCredentials(credentials)
//                 .build();

//         FirebaseApp.initializeApp(options);
//     }
// }

