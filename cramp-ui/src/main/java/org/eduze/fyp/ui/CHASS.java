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

import org.eduze.fyp.api.State;
import org.eduze.fyp.api.StateManager;
import org.eduze.fyp.api.annotations.AnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CHASS {

    private static final Logger logger = LoggerFactory.getLogger(CHASS.class);

    private static final String ROOT_CONFIG = "etc/spring.xml";

    private static CHASS chass;
    private ApplicationContext applicationContext;
    private StateManager stateManager = new StateManager(State.STOPPED);

    private CHASS() {
        applicationContext = new ClassPathXmlApplicationContext(ROOT_CONFIG);
    }

    public void start() {
        stateManager.checkState(State.STOPPED);
        stateManager.setState(State.STARTING);

        logger.debug("Starting auto annotated beans");
        try {
            AnnotationUtils.startAnnotatedElements(applicationContext);
            logger.info("Started auto annotated beans");
            Runtime.getRuntime().addShutdownHook(new Thread(CHASS.this::stop));
        } catch (Exception e) {
            logger.error("Error occurred when starting", e);
            stop();
        }
        stateManager.setState(State.STARTED);
    }

    public void stop() {
        stateManager.checkState(State.STARTED, State.STARTING);
        stateManager.setState(State.STOPPING);

        logger.debug("Stopping auto annotated beans");
        try {
            AnnotationUtils.stopAnnotatedElements(applicationContext);
            logger.debug("Stopped auto annotated beans");
        } catch (Exception e) {
            logger.error("Error occurred when stopping application", e);
        }
        stateManager.setState(State.STOPPED);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public static synchronized CHASS getInstance() {
        if (chass == null) {
            chass = new CHASS();
        }
        return chass;
    }

    public static void main(String[] args) {
        CHASS chass = CHASS.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(chass::stop));
        chass.start();
    }
}
