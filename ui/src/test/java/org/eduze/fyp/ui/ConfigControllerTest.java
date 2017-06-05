/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui;

import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.core.api.Point;
import org.eduze.fyp.core.api.PointMapping;
import org.eduze.fyp.restapi.resources.Camera;
import org.eduze.fyp.restapi.resources.CameraView;
import org.eduze.fyp.restapi.resources.MapConfiguration;
import org.eduze.fyp.restapi.util.ImageUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigControllerTest extends AbstractTestCase {

    private static final int CAMERA_ID = 1;
    private static final String MAP_IMAGE_PATH = "map.jpg";

    @Test
    public void getCameraIdTest() {
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .host("localhost")
                .port(8085)
                .path("v1")
                .path("config")
                .path("cameraId");


        Camera camera = client.target(builder)
                .request(MediaType.APPLICATION_JSON)
                .get(Camera.class);

        Assert.assertNotNull(camera);
        Assert.assertTrue(camera.getId() > 0);
    }

    @Test
    public void postCameraViewTest() throws IOException {
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .host("localhost")
                .port(8085)
                .path("v1")
                .path("config")
                .path("cameraView");

        Camera camera = new Camera(CAMERA_ID);
        BufferedImage mapImage = null;
        try (InputStream stream = new FileInputStream(MAP_IMAGE_PATH)) {
            mapImage = ImageIO.read(stream);
        }
        byte[] bytes = ImageUtils.bufferedImageToByteArray(mapImage, "jpg");

        CameraView cameraView = new CameraView();
        cameraView.setCamera(camera);
        cameraView.setViewBytes(bytes);

        Response response = client.target(builder)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(cameraView));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        BufferedImage cameraViewImage = ANALYTICS_ENGINE.getConfigurationManager().getCameraView(CAMERA_ID);

        Assert.assertEquals(cameraViewImage.getHeight(), mapImage.getHeight());
        Assert.assertEquals(cameraViewImage.getWidth(), mapImage.getWidth());
    }

    @Test
    public void getMapTest() throws IOException {
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .path("v1")
                .path("config")
                .host("localhost")
                .path("1")
                .port(8085);

        ConfigurationManager configurationManager = ANALYTICS_ENGINE.getConfigurationManager();
        PointMapping mapping = new PointMapping();

        mapping.addWorldSpacePoint(new Point(12.4, 45.5));
        mapping.addWorldSpacePoint(new Point(125.4, 45.5));
        mapping.addWorldSpacePoint(new Point(85.4, 145.5));
        mapping.addWorldSpacePoint(new Point(456.4, 845.5));

        mapping.addScreenSpacePoint(new Point(456.4, 845.5));
        mapping.addScreenSpacePoint(new Point(452.2, 845.5));
        mapping.addScreenSpacePoint(new Point(752.0, 845.5));
        mapping.addScreenSpacePoint(new Point(165.2, 845.5));

        configurationManager.addPointMapping(1, mapping);

        MapConfiguration mapConfiguration = client.target(builder)
                .request(MediaType.APPLICATION_JSON)
                .get(MapConfiguration.class);

        BufferedImage receivedMap = ImageUtils.byteArrayToBufferedImage(mapConfiguration.getMapImage());
        BufferedImage mapImage = null;
        try (InputStream stream = new FileInputStream(MAP_IMAGE_PATH)) {
            mapImage = ImageIO.read(stream);
        }

        Assert.assertEquals(receivedMap.getHeight(), mapImage.getHeight());
        Assert.assertEquals(receivedMap.getWidth(), mapImage.getWidth());
    }

    private void showImage(BufferedImage image) {
        JFrame frame = new JFrame();

        JLabel imageLabel = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(imageLabel, BorderLayout.CENTER);
        frame.setSize(image.getWidth(), image.getHeight());
        frame.setVisible(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(imageLabel);
        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
