/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core;

import org.eduze.fyp.core.api.AnalyticsEngine;

public abstract class AnalyticsEngineFactory {

    public static AnalyticsEngine getAnalyticsEngine() {
        return AnalyticsEngineImpl.getInstance();
    }
}
