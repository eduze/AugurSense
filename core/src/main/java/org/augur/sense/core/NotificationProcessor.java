/*
 * <Paste your header here>
 */
package org.augur.sense.core;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.augur.sense.api.Condition;
import org.augur.sense.api.config.Startable;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.Condition;
import org.augur.sense.api.annotations.AutoStart;
import org.augur.sense.api.annotations.Mode;
import org.augur.sense.api.config.Startable;
import org.augur.sense.api.listeners.ProcessedMapListener;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.resources.Notification;
import org.augur.sense.api.resources.PersonSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoStart(mode = Mode.ACTIVE)
public class NotificationProcessor implements ProcessedMapListener, Startable {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);

    private static final String DATABASE_URL = "https://augur-bia.firebaseio.com/";

    private ExecutorService executor;
    private List<Condition> conditions;
    private DatabaseReference databaseReference;

    @Override
    public void start() {
        executor = Executors.newSingleThreadExecutor();

        // Initialize Firebase
        try {
            // [START initialize]
            FileInputStream serviceAccount = new FileInputStream("etc/service-account.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(DATABASE_URL)
                    .build();
            FirebaseApp.initializeApp(options);
            // [END initialize]
        } catch (IOException e) {
            logger.error("Unable to create firebase app", e);
        }

        // Shared Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        logger.info("Notification processor started successfully");
    }

    @Override
    public void mapProcessed(CameraGroup cameraGroup, List<List<PersonSnapshot>> snapshots) {
        executor.submit(() -> process(cameraGroup, snapshots));
    }

    private void process(CameraGroup cameraGroup, List<List<PersonSnapshot>> snapshots) {
        List<Notification> notifications = conditions.stream()
                .map(condition -> condition.evaluate(cameraGroup, snapshots))
                .reduce(new ArrayList<>(), (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                });

        notifications.forEach(notification -> {
            logger.debug("Sending notification: {}", notification);
            databaseReference.child("notifications").push()
                    .setValueAsync(notification);
        });
    }

    @Override
    public void stop() {
        executor.shutdownNow();
        logger.info("Notification processor stopped");
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
}
