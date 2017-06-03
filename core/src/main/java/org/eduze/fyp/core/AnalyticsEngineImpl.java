/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core;

import org.eduze.fyp.core.api.AnalyticsEngine;
import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.core.config.InMemoryConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the analytics engine. This will start all the other required components for the analytics engine to run.
 *
 * @author Imesha Sudasingha
 */
public class AnalyticsEngineImpl extends AnalyticsEngine {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsEngineImpl.class);

    private static AnalyticsEngineImpl instance;

    private ConfigurationManager configurationManager;

    private AnalyticsEngineImpl() {
        configurationManager = new InMemoryConfigurationManager();
    }

    public void doStart() {
        logger.debug("Starting Analytics Engine");
        Runtime.getRuntime().addShutdownHook(new Thread(AnalyticsEngineImpl.this::stop));

        logger.info("Analytics Engine started ...");
    }

    public void doStop() {
        logger.debug("Stopping Analytics Engine");

        logger.info("Analytics Engine Stopped ...");
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Getter for the {@link AnalyticsEngineImpl} instance
     *
     * @return instance
     */
    static AnalyticsEngine getInstance() {
        if (instance == null) {
            instance = new AnalyticsEngineImpl();
        }
        return instance;
    }
}
