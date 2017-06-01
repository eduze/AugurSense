/*
 * Copyright to Eduze@UoM 2017
 */

package org.eduze.fyp.restapi.util;

import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RequestLogger extends AbstractNCSARequestLog {

    private static final Logger logger = LoggerFactory.getLogger(RequestLogger.class);

    protected boolean isEnabled() {
        return true;
    }

    public void write(String request) throws IOException {
        logger.debug("Request : {}", request);
    }
}
