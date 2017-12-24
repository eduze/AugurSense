/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.eduze.fyp.ui;

import org.eduze.fyp.api.AnalyticsEngine;
import org.eduze.fyp.ui.rest.RestServer;
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
        chass = CHASS.getInstance();
        chass.start();
    }

    @AfterClass
    public static void tearDown() {
        chass.stop();
    }
}
