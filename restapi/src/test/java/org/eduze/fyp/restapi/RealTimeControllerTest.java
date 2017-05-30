/*
 * Copyright to Eduze@UoM 2017
 */

package org.eduze.fyp.restapi;

import org.eduze.fyp.restapi.resources.Camera;
import org.eduze.fyp.restapi.resources.FrameInfo;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RealTimeControllerTest {

    private static final String ENDPOINT_URL = "http://localhost:8085/api/v1/realtime";

    private static final RestServer restServer = new RestServer();

    @BeforeClass
    public static void setUp() {
        restServer.start();
    }

    @Test
    public void testPostFrameInfo() {
        Client client = JerseyClientBuilder.newClient();
        WebTarget target = client.target(ENDPOINT_URL);


        float coordinates[][] = new float[5][2];
        coordinates[1][0] = 12.55f;
        coordinates[1][1] = 12.55f;

        FrameInfo frameInfo = new FrameInfo();
        frameInfo.setCamera(new Camera(1));
        frameInfo.setTimestamp(System.currentTimeMillis());
        frameInfo.setCoordinates(coordinates);

        Response response = target.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(frameInfo));

        System.out.println(response.getStatus());
    }

    @AfterClass
    public static void tearDown() {
        if (restServer.isRunning()) {
            restServer.stop();
        }
    }
}
