/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui;

import org.eduze.fyp.api.AnalyticsEngine;
import org.eduze.fyp.rest.RestServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public abstract class AbstractTestCase {

    private static CHASS chass;

    protected final AnalyticsEngine ANALYTICS_ENGINE;
    protected final RestServer REST_SERVER;

    protected final Logger logger;
    private ApplicationContext applicationContext;

    public AbstractTestCase() {
        logger = LoggerFactory.getLogger(getClass());

        ANALYTICS_ENGINE = chass.getApplicationContext().getBean(AnalyticsEngine.class);
        REST_SERVER = chass.getApplicationContext().getBean(RestServer.class);
    }

    @BeforeClass
    public static void setUp() {
        chass = new CHASS();
        chass.start();
    }

    @AfterClass
    public static void tearDown() {
        chass.stop();
    }
}
