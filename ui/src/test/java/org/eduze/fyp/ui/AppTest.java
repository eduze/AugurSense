/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui;

import org.eduze.fyp.core.api.AnalyticsEngineFactory;
import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.core.api.Point;
import org.eduze.fyp.restapi.resources.Camera;
import org.eduze.fyp.restapi.resources.FrameInfo;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppTest {

    private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

    private static final String[] views = new String[]{"views/view1.png", "views/view2.jpg"};

    public static void main(String[] args) throws IOException {
        ConfigurationManager configurationManager = AnalyticsEngineFactory.getAnalyticsEngine().getConfigurationManager();
        ExecutorService executorService = Executors.newFixedThreadPool(views.length);

        for (int i = 0; i < views.length; i++) {
            try (InputStream inputStream = new FileInputStream(views[i])) {
                BufferedImage cameraView = ImageIO.read(inputStream);
                configurationManager.setCameraView(i + 1, cameraView);
            }
            executorService.submit(new CameraSimulator(i + 1,
                    configurationManager.getMap().getWidth(),
                    configurationManager.getMap().getHeight()));
        }

        App.main(new String[0]);
        executorService.shutdownNow();
    }

    private static class CameraSimulator implements Runnable {

        private int cameraId;
        private int mapWidth;
        private int mapHeight;
        private WebTarget target;
        private List<Point> coordinates = new ArrayList<>();
        private Random random = new Random();


        private CameraSimulator(int cameraId, int mapWidth, int mapHeight) {
            this.cameraId = cameraId;
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;

            Client client = JerseyClientBuilder.createClient();
            UriBuilder builder = UriBuilder.fromPath("api")
                    .scheme("http")
                    .path("v1")
                    .path("realtime")
                    .host("localhost")
                    .port(8085);

            target = client.target(builder);

            int pointCount = random.nextInt(5) + 1;
            for (int i = 0; i < pointCount; i++) {
                double x = random.nextInt(mapWidth);
                double y = random.nextInt(mapHeight);
                coordinates.add(new Point(x, y));
            }
        }

        @Override
        public void run() {
            while (true) {
                coordinates.forEach(point -> {
                    double x = point.getX() + (random.nextBoolean() ? 1 : -1) * random.nextInt(10);
                    double y = point.getY() + (random.nextBoolean() ? 1 : -1) * random.nextInt(10);
                    point.setX(x < mapWidth ? x : 0);
                    point.setY(y < mapHeight ? y : 0);
                });

                Camera camera = new Camera(cameraId);

                FrameInfo frameInfo = new FrameInfo();
                frameInfo.setCamera(camera);
                frameInfo.setTimestamp(System.currentTimeMillis());
                frameInfo.setCoordinates(coordinates);

                try {
                    Response response = target.request(MediaType.APPLICATION_JSON)
                            .post(Entity.json(frameInfo));

                    response.close();

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted. Exiting");
                    break;
                } catch (ProcessingException ignored) {
                }
            }
        }

        public int getCameraId() {
            return cameraId;
        }
    }
}
