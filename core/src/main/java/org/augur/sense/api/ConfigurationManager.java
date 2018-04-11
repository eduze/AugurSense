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
package org.augur.sense.api;

import org.augur.sense.api.config.Startable;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.PointMapping;
import org.augur.sense.api.config.Startable;
import org.augur.sense.api.listeners.ConfigurationListener;
import org.augur.sense.api.model.CameraConfig;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.PointMapping;
import org.augur.sense.api.model.Zone;

import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for {@link AnalyticsEngine}'s Configuration Manager
 *
 * @author Imesha Sudasingha
 */
public interface ConfigurationManager extends Startable {

    /**
     * Adds a new camera configuration.
     *
     * @param cameraConfig {@link CameraConfig} to be added
     */
    void addCameraConfig(CameraConfig cameraConfig);

    /**
     * Adds the 2D to 3D point mappings corresponding to a given camera
     *
     * @param cameraId camera ID
     * @param mapping  list of mappings of 4 points from 2D space to 3D space
     */
    void addPointMapping(int cameraId, PointMapping mapping);

    int getNextCameraId();

    CameraConfig getCameraConfig(int cameraId);

    Map<Integer, CameraConfig> getCameraConfigs();

    BufferedImage getMap();

    PointMapping getPointMapping(int cameraId);

    Set<Integer> getCameraIds();

    Set<InetSocketAddress> getCameraIpAndPorts();

    int getNumberOfCameras();

    void addConfigurationListener(ConfigurationListener listener);

    void removeConfigurationListener(ConfigurationListener listener);

    boolean isConfigured();

    List<Zone> getZones();

    Map<Integer, CameraGroup> getCameraGroups();
}
