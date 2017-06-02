/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui;

import org.eduze.fyp.core.AnalyticsEngine;
import org.eduze.fyp.restapi.RestServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestCase {

    protected static final AnalyticsEngine analyticsEngine = AnalyticsEngine.getInstance();
    protected static final RestServer restServer = RestServer.getInstance();

    protected final Logger logger;

    public AbstractTestCase() {
        logger = LoggerFactory.getLogger(getClass());
    }

    @BeforeClass
    public static void setUp() {
        analyticsEngine.start();
        restServer.start();
    }

    @AfterClass
    public static void tearDown() {
        restServer.stop();
        analyticsEngine.stop();
    }
}
