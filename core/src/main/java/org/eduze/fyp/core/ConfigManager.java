/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    public BufferedImage getMap() {
        return map;
    }

    public void setMap(BufferedImage map) {
        this.map = map;
    }
}
