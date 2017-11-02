/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.eduze.fyp.api;

import org.eduze.fyp.api.config.Startable;
import org.eduze.fyp.api.listeners.ConfigurationListener;
import org.eduze.fyp.api.resources.PointMapping;
import org.eduze.fyp.impl.db.model.Zone;

import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for {@link AnalyticsEngine}'s Configuration Manager
 *
 * @author Imesha Sudasingha
 */
public interface ConfigurationManager extends Startable {

    void setCameraView(int cameraId, BufferedImage view);

    void setCameraIpAndPort(int cameraId, String ipAndPort) throws UnknownHostException;

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

    PointMapping getPointMapping(int cameraId);

    Set<Integer> getCameraIds();

    Set<InetSocketAddress> getCameraIpAndPorts();

    int getNumberOfCameras();

    void addConfigurationListener(ConfigurationListener listener);

    void removeConfigurationListener(ConfigurationListener listener);

    boolean isConfigured();

    List<Zone> getZones();
}
