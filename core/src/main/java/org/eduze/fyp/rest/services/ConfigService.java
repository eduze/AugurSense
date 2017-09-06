/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.eduze.fyp.rest.services;

import org.eduze.fyp.api.AnalyticsEngine;
import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.rest.resources.Camera;
import org.eduze.fyp.rest.resources.CameraConfig;
import org.eduze.fyp.rest.resources.MapConfiguration;
import org.eduze.fyp.rest.util.ImageUtils;
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
     * @param cameraConfig {@link CameraConfig} instance to be configured
     * @throws IOException
     */
    public void configureCameraView(CameraConfig cameraConfig) throws IOException {
        BufferedImage view = ImageUtils.byteArrayToBufferedImage(cameraConfig.getViewBytes());
        configurationManager.setCameraView(cameraConfig.getCamera().getId(), view);
        configurationManager.setCameraIpAndPort(cameraConfig.getCamera().getId(), cameraConfig.getIpAndPort());
    }

    /**
     * Get the floor plan or map of the enclosed are which the {@link AnalyticsEngine} is going to cover
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
