/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi;

import org.apache.commons.io.IOUtils;
import org.eduze.fyp.restapi.resources.Camera;
import org.eduze.fyp.restapi.resources.CameraView;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class ConfigControllerTest extends AbstractTestCase {

    @Test
    public void postCameraView() throws IOException {
        Client client = JerseyClientBuilder.createClient();

        UriBuilder builder = UriBuilder.fromPath("api")
                .scheme("http")
                .path("v1")
                .path("config")
                .host("localhost")
                .port(8085);

        WebTarget target = client.target(builder);

        Camera camera = new Camera(1);

        byte[] bytes = IOUtils.toByteArray(new FileInputStream("map.jpg"));
        String base64 = Base64.getEncoder().encodeToString(bytes);

        CameraView cameraView = new CameraView();
        cameraView.setCamera(camera);
        cameraView.setView(bytes);

        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.json(cameraView));

//        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }
}
