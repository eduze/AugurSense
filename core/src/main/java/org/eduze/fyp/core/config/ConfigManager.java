/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core.config;

import org.eduze.fyp.core.AnalyticsEngine;
import org.eduze.fyp.core.util.PointMapping;
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
 * Class responsible for managing all the configurations of the {@link AnalyticsEngine}
 *
 * @author Imesha Sudasingha
 */
public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private static final String PROPERTIES_FILE = "analytics.properties";

    private Properties properties;
    private BufferedImage map;
    private Map<Integer, BufferedImage> cameraViews = new ConcurrentHashMap<>();
    private Map<Integer, PointMapping> pointMappings = new HashMap<>();

    public ConfigManager() {
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

    public void setCameraView(int cameraId, BufferedImage view) {
        cameraViews.put(cameraId, view);
    }

    /**
     * Adds the 2D to 3D point mappings corresponding to a given camera
     *
     * @param cameraId camera ID
     * @param mappings list of mappings of 4 points from 2D space to 3D space
     */
    public void addPointMapping(int cameraId, PointMapping mappings) {
        pointMappings.put(cameraId, mappings);
    }

    public synchronized int getNextCameraId() {
        Optional<Integer> max = cameraViews.keySet().stream().max(Comparator.naturalOrder());
        return max.map(integer -> integer + 1).orElse(1);
    }

    public BufferedImage getCameraView(int cameraId) {
        return cameraViews.get(cameraId);
    }

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

    public Map<Integer, PointMapping> getPointMappings() {
        return pointMappings;
    }
}
