/*
 * Copyright to Eduze@UoM 2017
 */

package org.eduze.fyp.ui;

import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.rest.resources.Camera;
import org.eduze.fyp.rest.resources.FrameInfo;
import org.eduze.fyp.rest.util.ImageUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
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

public class RealTimeControllerTest extends AbstractTestCase {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeControllerTest.class);

    private static final String PERSON_IMAGE = "src/test/resources/person.png";

    @Test
    public void testGetFrameInfo() {
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .path("v1")
                .path("realtime")
                .host("localhost")
                .port(8085);

        WebTarget target = client.target(builder);

        logger.debug("Sending request");
        Response response = target.request(MediaType.APPLICATION_JSON).get();

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPostFrameInfo() throws IOException {
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .path("v1")
                .path("realtime")
                .host("localhost")
                .port(8085);

        WebTarget target = client.target(builder);

        long timestamp = System.currentTimeMillis();
        BufferedImage personImage = null;
        try (InputStream stream = new FileInputStream(PERSON_IMAGE)) {
            personImage = ImageIO.read(stream);
        }
        byte[] bytes = ImageUtils.bufferedImageToByteArray(personImage, "jpg");

        List<PersonCoordinate> coordinates = new ArrayList<>();
        coordinates.add(new PersonCoordinate(15.3, 863.5, timestamp, bytes));
        coordinates.add(new PersonCoordinate(15.3, 863.5, timestamp, bytes));
        coordinates.add(new PersonCoordinate(15.3, 863.5, timestamp, bytes));
        coordinates.add(new PersonCoordinate(15.3, 863.5, timestamp, bytes));

        Camera camera = new Camera(1);
        FrameInfo frameInfo = new FrameInfo();
        frameInfo.setCamera(camera);
        frameInfo.setTimestamp(System.currentTimeMillis());
        frameInfo.setPersonCoordinates(coordinates);

        logger.debug("Sending request");
        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.json(frameInfo));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }
}
