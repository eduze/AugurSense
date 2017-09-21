/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.eduze.fyp.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.State;
import org.eduze.fyp.api.resources.LocalMap;
import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.api.resources.PointMapping;
import org.eduze.fyp.rest.resources.Camera;
import org.eduze.fyp.rest.resources.CameraConfig;
import org.eduze.fyp.rest.util.ImageUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eduze.fyp.Constants.CAMERA_NOTIFICATION_PATH;

public class AppTest {

    private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

    private static final String[] views = new String[]{
            "src/test/resources/views/view1.png",
            "src/test/resources/views/view2.jpg"
    };

    private static final String[] data = new String[]{
            "src/test/resources/data/PETS09S2L1V1_Points.txt",
            "src/test/resources/data/PETS09S2L1V5_Points.txt"
    };

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread mainThread = new Thread(() -> App.main(args));
        mainThread.start();

        while (App.getInstance() == null) {
            logger.debug("Waiting ...");
            Thread.sleep(1000);
        }

        App.getInstance().getChass().getStateManager().waitFor(State.STARTED);
        ConfigurationManager configurationManager = App.getInstance().getChass()
                .getApplicationContext().getBean(ConfigurationManager.class);

        Set<CameraSimulator> simulators = new HashSet<>();
        for (int i = 0; i < views.length; i++) {
            Camera camera = setupCamera();
            int port = (8000 + camera.getId());

            Dimension dimension = setupCameraConfig(camera.getId(), views[i],
                    "localhost:" + port);

            configurationManager.addPointMapping(camera.getId(), new PointMapping());
            logger.debug("Camera-{} configured", camera.getId());

            CameraSimulator simulator = new CameraSimulator(camera.getId(), (int) dimension.getWidth(),
                    (int) dimension.getHeight(), data[i], port);
            simulator.start();
            simulators.add(simulator);
        }

        try {
            mainThread.join();
        } catch (InterruptedException ignored) {
        }

        simulators.forEach(CameraSimulator::stop);
    }

    private static Camera setupCamera() {
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .host("localhost")
                .port(8085)
                .path("v1")
                .path("config")
                .path("cameraId");

        return client.target(builder)
                .request(MediaType.APPLICATION_JSON)
                .get(Camera.class);
    }

    private static Dimension setupCameraConfig(int cameraId, String viewImgPath, String address) throws IOException {
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .host("localhost")
                .port(8085)
                .path("v1")
                .path("config")
                .path("cameraConfig");

        Camera camera = new Camera(cameraId);
        BufferedImage viewImage = null;
        try (InputStream stream = new FileInputStream(viewImgPath)) {
            viewImage = ImageIO.read(stream);
        }
        byte[] bytes = ImageUtils.bufferedImageToByteArray(viewImage);

        CameraConfig cameraConfig = new CameraConfig();
        cameraConfig.setCamera(camera);
        cameraConfig.setViewBytes(bytes);
        cameraConfig.setIpAndPort(address);

        Response response = client.target(builder)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(cameraConfig));

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new IllegalStateException("Unable to post camera view");
        }

        return new Dimension(viewImage.getWidth(), viewImage.getHeight());
    }

    private static class CameraSimulator implements HttpHandler {
        private int cameraId;
        private int mapWidth;
        private int mapHeight;
        private List<String> lines;
        private int lineNumber = 0;
        private Random random = new Random();
        private ObjectMapper mapper = new ObjectMapper();

        private HttpServer server;

        private CameraSimulator(int cameraId, int mapWidth, int mapHeight, String dataFile, int serverPort) throws IOException {
            this.cameraId = cameraId;
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;
            lines = Files.readAllLines(new File(dataFile).toPath());
            server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            server.createContext(CAMERA_NOTIFICATION_PATH, this);
        }

        public void start() {
            server.start();
            logger.debug("Server {} started", this.server.getAddress().toString());
        }

        public void stop() {
            server.stop(0);
        }


        public LocalMap processNextMap(long timestamp) {
            String line = lines.get(lineNumber);
            lineNumber++;
            if (lineNumber == lines.size()) {
                lineNumber = 0;
            }

            List<Double> points = Stream.of(line.split(","))
                    .mapToDouble(Double::parseDouble)
                    .boxed()
                    .collect(Collectors.toList());

            List<PersonCoordinate> coordinates = new ArrayList<>();
            PersonCoordinate personCoordinate = null;
            for (int i = 0; i < points.size(); i++) {
                if (personCoordinate == null) {
                    personCoordinate = new PersonCoordinate();
                    personCoordinate.setX(points.get(i));
                } else {
                    personCoordinate.setY(points.get(i));
                }

                if (i % 2 != 0) {
                    coordinates.add(personCoordinate);
                    personCoordinate = null;
                }
            }

            coordinates.forEach(coordinate -> {
                double x = coordinate.getX();
                double y = coordinate.getY();

                x = x < 0 ? 0 : x;
                x = x > mapWidth ? mapHeight : x;

                y = y < 0 ? 0 : y;
                y = y > mapHeight ? mapHeight : y;

                coordinate.setX(x);
                coordinate.setY(y);
                coordinate.setTimestamp(timestamp);
            });

            LocalMap map = new LocalMap();
            map.setCameraId(cameraId);
            map.setTimestamp(timestamp);
            map.setPersonCoordinates(coordinates);
            return map;
        }

        public int getCameraId() {
            return cameraId;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            logger.debug("Received notification {}", httpExchange.getRequestURI().getPath());
            long timestamp = Long.parseLong(httpExchange.getRequestURI().getPath().split("/")[2]);
            logger.debug("Generating local map for timestamp {}", timestamp);
            LocalMap map = processNextMap(timestamp);
            String response = mapper.writeValueAsString(map);
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.flush();
            os.close();
        }
    }
}
