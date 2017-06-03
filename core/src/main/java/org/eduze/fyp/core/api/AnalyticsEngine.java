/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core.api;

/**
 * Interface for Analytics Engine core
 *
 * @author Imesha Sudasingha
 */
public abstract class AnalyticsEngine {

    public void start() {
        doStart();
    }

    public void stop() {
        doStop();
    }

    protected abstract void doStart();

    protected abstract void doStop();

    public abstract ConfigurationManager getConfigurationManager();

    public abstract DataCollector getDataCollector();

    public abstract DataProcessor getDataProcessor();
}
