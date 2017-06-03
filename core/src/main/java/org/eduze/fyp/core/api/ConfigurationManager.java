/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core.api;

import org.eduze.fyp.core.api.listeners.ConfigurationListener;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Interface for {@link AnalyticsEngine}'s Configuration Manager
 *
 * @author Imesha Sudasingha
 */
public interface ConfigurationManager {

    void setCameraView(int cameraId, BufferedImage view);

    /**
     * Adds the 2D to 3D point mappings corresponding to a given camera
     *
     * @param cameraId camera ID
     * @param mapping  list of mappings of 4 points from 2D space to 3D space
     */
    void addPointMapping(int cameraId, PointMapping mapping);

    int getNextCameraId();

    BufferedImage getCameraView(int cameraId);

    Map<Integer, BufferedImage> getCameraViews();

    BufferedImage getMap();

    Map<Integer, PointMapping> getPointMappings();

    int getNumberOfCameras();

    void addConfigurationListener(ConfigurationListener listener);

    void removeConfigurationListener(ConfigurationListener listener);
}
