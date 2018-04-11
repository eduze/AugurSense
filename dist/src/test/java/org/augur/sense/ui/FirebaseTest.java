/*
 * <Paste your header here>
 */
package org.augur.sense.ui;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.augur.sense.api.resources.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

public class FirebaseTest {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseTest.class);

    private static final String DATABASE_URL = "https://augur-bia.firebaseio.com/";

    public static void main(String[] args) throws InterruptedException {
        // Initialize Firebase
        try {
            // [START initialize]
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/etc/service-account.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(DATABASE_URL)
                    .build();
            FirebaseApp.initializeApp(options);
            // [END initialize]
        } catch (IOException e) {
            System.out.println("ERROR: invalid service account credentials. See README.");
            System.out.println(e.getMessage());

            System.exit(1);
        }

        // Shared Database reference
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        DatabaseReference notifications = database.child("notifications");

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Notification notification = new Notification("Notification " + i,
                    "Zone " + i + " is crowded", new Date().getTime());

            ApiFuture<Void> future = notifications.push()
                    .setValueAsync(notification);

            while (!future.isDone()) {
                //                logger.debug("{}: Not added yet", notification);
                Thread.sleep(1000);
            }

            logger.debug("Added notification: {}", i);
            try {
                Thread.sleep(random.nextInt(10000) + 3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
