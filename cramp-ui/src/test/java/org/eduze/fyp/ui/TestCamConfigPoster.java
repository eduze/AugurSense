
package org.eduze.fyp.ui;

import org.eduze.fyp.api.resources.Camera;
import org.eduze.fyp.api.model.CameraConfig;
import org.eduze.fyp.api.resources.Point;
import org.eduze.fyp.api.model.PointMapping;
import org.eduze.fyp.api.util.ImageUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class TestCamConfigPoster {

    public static void main(String[] args) throws IOException {
        Client client = JerseyClientBuilder.createClient();

        for (int j = 0; j < 3; j++) {
            System.out.println("Cam - " + j);
            UriBuilder builder = UriBuilder.fromPath("api")
                    .scheme("http")
                    .host("localhost")
                    .port(8000)
                    .path("v1")
                    .path("config")
                    .path("cameraId");

            Camera camera = client.target(builder)
                    .request(MediaType.APPLICATION_JSON)
                    .get(Camera.class);

            builder = UriBuilder.fromPath("api")
                    .scheme("http")
                    .host("localhost")
                    .port(8000)
                    .path("v1")
                    .path("config")
                    .path("cameraConfig");

            BufferedImage viewImage = null;
            try (InputStream stream = new FileInputStream("views/view1.png")) {
                viewImage = ImageIO.read(stream);
            }
            byte[] bytes = ImageUtils.bufferedImageToByteArray(viewImage, "jpg");

            PointMapping pointMapping = new PointMapping();
            Random random = new Random();
            for (int i = 0; i < 4; i++) {
                int x = random.nextInt(viewImage.getWidth());
                int y = random.nextInt(viewImage.getHeight());
                pointMapping.addScreenSpacePoint(new Point(x, y));
                x = random.nextInt(300);
                y = random.nextInt(300);
                pointMapping.addWorldSpacePoint(new Point(x, y));
            }

            CameraConfig cameraConfig = new CameraConfig();
            cameraConfig.setCameraId(camera.getId());
            cameraConfig.setView(bytes);
            cameraConfig.setPointMapping(pointMapping);
            cameraConfig.setIpAndPort("localhost:80");

            String response = client.target(builder)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(cameraConfig), String.class);
            System.out.println(response);
        }
    }
}
