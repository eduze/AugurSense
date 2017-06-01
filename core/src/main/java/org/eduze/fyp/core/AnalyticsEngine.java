/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core;

import org.eduze.fyp.restapi.RestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the analytics engine. This will start all the other required components for the analytics engine to run.
 *
 * @author Imesha Sudasingha
 */
public class AnalyticsEngine {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsEngine.class);

    private RestServer restServer;
    private ConfigManager configManager;

    public AnalyticsEngine() {
        restServer = new RestServer();
        configManager = new ConfigManager();
    }

    public void start() {
        logger.debug("Starting Analytics Engine");
        restServer.start();
        logger.info("Analytics Engine started ...");
    }

    public void stop() {
        logger.debug("Stopping Analytics Engine");
        if (restServer.isRunning()) {
            logger.debug("Stopping Rest Server");
            restServer.stop();
            logger.info("Rest Server stopped ...");
        }

        logger.info("Analytics Engine Stopped ...");
    }

    public RestServer getRestServer() {
        return restServer;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
