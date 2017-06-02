/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core;

import org.eduze.fyp.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the analytics engine. This will start all the other required components for the analytics engine to run.
 *
 * @author Imesha Sudasingha
 */
public class AnalyticsEngine {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsEngine.class);

    private static AnalyticsEngine instance;

    private ConfigManager configManager;

    private AnalyticsEngine() {
        configManager = new ConfigManager();
    }

    public void start() {
        logger.debug("Starting Analytics Engine");
        Runtime.getRuntime().addShutdownHook(new Thread(AnalyticsEngine.this::stop));

        logger.info("Analytics Engine started ...");
    }

    public void stop() {
        logger.debug("Stopping Analytics Engine");

        logger.info("Analytics Engine Stopped ...");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Getter for the {@link AnalyticsEngine} instance
     *
     * @return instance
     */
    public static AnalyticsEngine getInstance() {
        if (instance == null) {
            instance = new AnalyticsEngine();
        }
        return instance;
    }
}
