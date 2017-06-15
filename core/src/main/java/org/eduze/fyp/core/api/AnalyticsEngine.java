/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core.api;

import org.eduze.fyp.core.api.config.Startable;

/**
 * Interface for Analytics Engine core
 *
 * @author Imesha Sudasingha
 */
public abstract class AnalyticsEngine implements Startable{

    public void start() {
        doStart();
    }

    public void stop() {
        doStop();
    }

    protected abstract void doStart();

    protected abstract void doStop();

    public abstract ConfigurationManager getConfigurationManager();

    public abstract MapCollector getMapCollector();

    public abstract MapProcessor getMapProcessor();
}
