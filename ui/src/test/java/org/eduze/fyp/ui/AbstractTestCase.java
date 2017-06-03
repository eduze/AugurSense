/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui;

import org.eduze.fyp.core.AnalyticsEngineFactory;
import org.eduze.fyp.core.api.AnalyticsEngine;
import org.eduze.fyp.restapi.RestServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestCase {

    protected static final AnalyticsEngine ANALYTICS_ENGINE = AnalyticsEngineFactory.getAnalyticsEngine();
    protected static final RestServer REST_SERVER = RestServer.getInstance();

    protected final Logger logger;

    public AbstractTestCase() {
        logger = LoggerFactory.getLogger(getClass());
    }

    @BeforeClass
    public static void setUp() {
        ANALYTICS_ENGINE.start();
        REST_SERVER.start();
    }

    @AfterClass
    public static void tearDown() {
        REST_SERVER.stop();
        ANALYTICS_ENGINE.stop();
    }
}
