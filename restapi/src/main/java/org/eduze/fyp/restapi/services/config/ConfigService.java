/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi.services.config;

import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.restapi.resources.Camera;
import org.eduze.fyp.restapi.resources.CameraView;
import org.eduze.fyp.restapi.resources.MapConfiguration;
import org.eduze.fyp.restapi.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.NotFoundException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ConfigService {

    @Autowired
    private ConfigurationManager configurationManager;

    /**
     * Obtain an ID for camera. This must be called and an ID should be obtained in order to call any other method
     *
     * @return camera ID
     */
    public Camera getCameraId() {
        int cameraId = configurationManager.getNextCameraId();
        return new Camera(cameraId);
    }

    /**
     * Adds a camera view to the configuration. The camera view submitted here will be used lataer for point
     * configuration
     *
     * @param cameraView {@link CameraView} instance to be configured
     * @throws IOException
     */
    public void configureCameraView(CameraView cameraView) throws IOException {
        BufferedImage view = ImageUtils.byteArrayToBufferedImage(cameraView.getViewBytes());
        configurationManager.setCameraView(cameraView.getCamera().getId(), view);
    }

    /**
     * Get the floor plan or map of the enclosed are which the {@link org.eduze.fyp.core.api.AnalyticsEngine} is going to cover
     *
     * @param cameraId camera ID
     * @return byte array of the map image
     */
    public MapConfiguration getMap(int cameraId) throws IOException {
        if (!configurationManager.isConfigured()) {
            return null;
        }

        BufferedImage map = configurationManager.getMap();
        byte[] mapImageBytes = ImageUtils.bufferedImageToByteArray(map);

        MapConfiguration mapConfiguration = new MapConfiguration();
        mapConfiguration.setMapImage(mapImageBytes);
        mapConfiguration.setMapping(configurationManager.getPointMapping(cameraId));
        mapConfiguration.setMapHeight(map.getHeight());
        mapConfiguration.setMapWidth(map.getWidth());

        return mapConfiguration;
    }

    public byte[] getCameraView(int cameraId) throws IOException {
        BufferedImage cameraView = configurationManager.getCameraView(cameraId);
        if (cameraView == null) {
            throw new NotFoundException("Camera view not found");
        }

        return ImageUtils.bufferedImageToByteArray(cameraView);
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }
}
