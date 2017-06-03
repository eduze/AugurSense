/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi.services.config;

import org.eduze.fyp.core.AnalyticsEngineFactory;
import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.restapi.resources.CameraView;
import org.eduze.fyp.restapi.resources.MapConfiguration;
import org.eduze.fyp.restapi.util.ImageUtils;

import javax.ws.rs.NotFoundException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ConfigService {

    private static final ConfigurationManager IN_MEMORY_CONFIGURATION_MANAGER =
            AnalyticsEngineFactory.getAnalyticsEngine().getConfigurationManager();

    /**
     * Adds a camera view to the configuration. The camera view submitted here will be used lataer for point
     * configuration
     *
     * @param cameraView {@link CameraView} instance to be configured
     * @throws IOException
     */
    public void configureCameraView(CameraView cameraView) throws IOException {
        BufferedImage view = ImageUtils.byteArrayToBufferedImage(cameraView.getViewBytes());
        IN_MEMORY_CONFIGURATION_MANAGER.setCameraView(cameraView.getCamera().getId(), view);
    }

    /**
     * Get the floor plan or map of the enclosed are which the {@link org.eduze.fyp.core.api.AnalyticsEngine} is going to cover
     *
     * @return byte array of the map image
     */
    public MapConfiguration getMap() throws IOException {
        BufferedImage map = IN_MEMORY_CONFIGURATION_MANAGER.getMap();
        byte[] mapImageBytes = ImageUtils.bufferedImageToByteArray(map);

        MapConfiguration mapConfiguration = new MapConfiguration();
        mapConfiguration.setMapImage(mapImageBytes);
        mapConfiguration.setMappings(IN_MEMORY_CONFIGURATION_MANAGER.getPointMappings());
        mapConfiguration.setMapHeight(map.getHeight());
        mapConfiguration.setMapWidth(map.getWidth());

        return mapConfiguration;
    }

    public byte[] getCameraView(int cameraId) throws IOException {
        BufferedImage cameraView = IN_MEMORY_CONFIGURATION_MANAGER.getCameraView(cameraId);
        if (cameraView == null) {
            throw new NotFoundException("Camera view not found");
        }

        return ImageUtils.bufferedImageToByteArray(cameraView);
    }
}
