/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core.api;

import org.eduze.fyp.core.AnalyticsEngineImpl;

public abstract class AnalyticsEngineFactory {

    public static AnalyticsEngine getAnalyticsEngine() {
        return AnalyticsEngineImpl.getInstance();
    }
}
