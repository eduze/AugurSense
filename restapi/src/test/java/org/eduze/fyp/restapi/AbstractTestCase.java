/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractTestCase {

    private static final RestServer restServer = new RestServer();

    @BeforeClass
    public static void setUp() {
        restServer.start();
    }

    @AfterClass
    public static void tearDown() {
        if (restServer.isRunning()) {
            restServer.stop();
        }
    }
}
