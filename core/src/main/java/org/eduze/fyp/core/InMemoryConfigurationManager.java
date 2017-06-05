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
package org.eduze.fyp.core;

import org.eduze.fyp.core.api.listeners.ConfigurationListener;
import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.core.api.PointMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.eduze.fyp.core.Constants.Properties.FLOOR_MAP_IMAGE;

/**
 * {@link ConfigurationManager} implementation which is doing all operations using in-memory data storage.
 *
 * @author Imesha Sudasingha
 */
public class InMemoryConfigurationManager implements ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryConfigurationManager.class);

    private static final String PROPERTIES_FILE = "analytics.properties";

    private Properties properties;
    private BufferedImage map;
    private Set<Integer> cameraIds = new HashSet<>();
    private Map<Integer, BufferedImage> cameraViews = new ConcurrentHashMap<>();
    private Map<Integer, PointMapping> pointMappings = new HashMap<>();
    private Set<ConfigurationListener> configurationListeners = new HashSet<>();

    public InMemoryConfigurationManager() {
        try {
            loadProperties();
        } catch (IOException e) {
            logger.error("Error occurred when loading configuration", e);
            throw new IllegalArgumentException("Unable to load properties", e);
        }

        String mapPath = properties.getProperty(FLOOR_MAP_IMAGE);
        logger.debug("Using map image : {}", mapPath);
        try {
            map = ImageIO.read(new FileInputStream(mapPath));
        } catch (IOException e) {
            logger.error("Unable to load the map image : {}", mapPath, e);
            throw new IllegalArgumentException("Unable to load the map image");
        }
    }

    private void loadProperties() throws IOException {
        properties = new Properties();
        InputStream in = new FileInputStream(PROPERTIES_FILE);
        properties.load(in);
    }

    public synchronized void setCameraView(int cameraId, BufferedImage view) {
        cameraViews.put(cameraId, view);
        notifyConfigurationChange();
    }

    public synchronized void addPointMapping(int cameraId, PointMapping mappings) {
        pointMappings.put(cameraId, mappings);
        notifyConfigurationChange();
    }

    public synchronized int getNextCameraId() {
        OptionalInt max = cameraIds.stream().mapToInt(Integer::intValue).max();
        int nextInt = 1;
        if (max.isPresent()) {
            nextInt = max.getAsInt() + 1;
        }

        cameraIds.add(nextInt);
        return nextInt;
    }

    public BufferedImage getCameraView(int cameraId) {
        return cameraViews.get(cameraId);
    }

    @Override
    public Map<Integer, BufferedImage> getCameraViews() {
        Map<Integer, BufferedImage> copyOfCameraViews = new HashMap<>();
        copyOfCameraViews.putAll(cameraViews);
        return copyOfCameraViews;
    }

    public BufferedImage getMap() {
        ColorModel cm = map.getColorModel();
        boolean isAlphaPreMultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = map.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPreMultiplied, null);
    }

    @Override
    public Map<Integer, PointMapping> getPointMappings() {
        return pointMappings;
    }

    @Override
    public PointMapping getPointMapping(int cameraId) {
        return pointMappings.get(cameraId);
    }

    @Override
    public Set<Integer> getCameraIds() {
        return cameraIds;
    }

    @Override
    public int getNumberOfCameras() {
        return cameraViews.entrySet().size();
    }

    @Override
    public synchronized void addConfigurationListener(ConfigurationListener listener) {
        configurationListeners.add(listener);
    }

    @Override
    public synchronized void removeConfigurationListener(ConfigurationListener listener) {
        configurationListeners.remove(listener);
    }

    @Override
    public boolean isConfigured() {
        return cameraIds.size() == pointMappings.keySet().size();
    }

    private void notifyConfigurationChange() {
        logger.debug("Notifying configuration change");
        configurationListeners.forEach(listener -> listener.configurationChanged(this));
    }
}
