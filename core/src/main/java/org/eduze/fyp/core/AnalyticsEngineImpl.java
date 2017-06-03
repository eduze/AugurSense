/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core;

import org.eduze.fyp.core.api.AnalyticsEngine;
import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.core.api.DataCollector;
import org.eduze.fyp.core.api.DataProcessor;
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
    private DataCollector dataCollector;
    private DataProcessor dataProcessor;

    private AnalyticsEngineImpl() {
        configurationManager = new InMemoryConfigurationManager();
        dataCollector = new InMemoryDataCollector(configurationManager.getNumberOfCameras());
        dataProcessor = new InMemoryDataProcessor();

        configurationManager.addConfigurationListener(dataCollector);
        dataCollector.addDataListener(dataProcessor);
    }

    @Override
    public void doStart() {
        logger.debug("Starting Analytics Engine");
        Runtime.getRuntime().addShutdownHook(new Thread(AnalyticsEngineImpl.this::stop));

        logger.info("Analytics Engine started ...");
    }

    @Override
    public void doStop() {
        logger.debug("Stopping Analytics Engine");

        logger.info("Analytics Engine Stopped ...");
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    @Override
    public DataCollector getDataCollector() {
        return dataCollector;
    }

    @Override
    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    /**
     * Getter for the {@link AnalyticsEngineImpl} instance
     *
     * @return instance
     */
    public static AnalyticsEngine getInstance() {
        if (instance == null) {
            instance = new AnalyticsEngineImpl();
        }
        return instance;
    }
}
