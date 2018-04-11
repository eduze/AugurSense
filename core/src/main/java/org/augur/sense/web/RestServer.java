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

package org.augur.sense.web;

import org.augur.sense.api.annotations.AutoStart;
import org.augur.sense.api.annotations.Mode;
import org.augur.sense.api.config.Startable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.PathResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class of the <pre>rest</pre> package. This class is using <pre>Jetty</pre> server to initialize the
 * REST server.
 *
 * @author Imesha Sudasingha
 */
@AutoStart(startOrder = 2, mode = Mode.PASSIVE)
public class RestServer implements Startable {

    private static final Logger logger = LoggerFactory.getLogger(RestServer.class);

    private static final String JETTY_CONFIG = "jetty.xml";
    private static final String CONTEXT_PATH = "/api/v1/*";

    private Set<Object> controllers = new HashSet<>();
    private Server jettyServer;
    private Server webServer;
    private boolean startWebServer = true;

    private RestServer(boolean startWebServer) {
        this.startWebServer = startWebServer;
    }

    /**
     * Starts the jetty server. This will also add a shutdown hook to make sure the server will go down properly.
     */
    @Override
    public void start() {
        startRestServer();
        logger.info("Servers started successfully");
    }

    private void startRestServer() {
        this.jettyServer = new Server(8000);

        ResourceConfig config = new ResourceConfig();
        controllers.forEach(config::register);

        // The filesystem paths we will map
        Path pwdPath = null;
        try {
            pwdPath = new File(System.getProperty("user.dir")).toPath().toRealPath();
        } catch (IOException e) {
            logger.error("Error occurred", e);
            throw new IllegalStateException("Error occurred", e);
        }

        ServletContextHandler context = new ServletContextHandler(jettyServer, null);
        context.setContextPath("/");
        context.setBaseResource(new PathResource(pwdPath));
        context.setSessionHandler(new SessionHandler());

        final ServletContainer servletContainer = new ServletContainer(config);
        ServletHolder servlet = new ServletHolder(servletContainer);
        context.addServlet(servlet, CONTEXT_PATH);

        /* Adding NG App to a default servlet */
        DefaultServlet defaultServlet = new DefaultServlet();
        ServletHolder servletHolder = new ServletHolder("default", defaultServlet);
        servletHolder.setInitParameter("dirAllowed", "true");
        URL path = this.getClass().getClassLoader().getResource("ng");
        try {
            servletHolder.setInitParameter("resourceBase", path.toURI().toASCIIString());
            context.addServlet(servletHolder, "/");
        } catch (Exception e) {
            logger.error("Error occurred when starting web server", e);
            //            throw new IllegalStateException("Unable to start web server", e);
        }

        jettyServer.setHandler(context);
        logger.debug("Starting REST server");
        try {
            jettyServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(RestServer.this::stop));
        } catch (Exception e) {
            logger.error("Error occurred when starting REST server due to : {}", e.getMessage());
            throw new IllegalStateException("Unable to start REST server", e);
        }
        logger.info("REST Server started successfully ...");
    }

    /**
     * Stops the jetty server
     */
    @Override
    public void stop() {
        if (jettyServer != null && jettyServer.isStarted()) {
            logger.info("Stopping REST Server ...");
            try {
                jettyServer.stop();
            } catch (Exception e) {
                logger.error("Error occurred when stopping the REST server due to : {}", e.getMessage());
            }
        }

        if (webServer != null && webServer.isStarted()) {
            logger.info("Stopping WEB Server ...");
            try {
                webServer.stop();
            } catch (Exception e) {
                logger.error("Error occurred when stopping the WEB server due to : {}", e.getMessage());
            }
        }
    }

    /**
     * Returns whether the server is running at the moment
     *
     * @return true if the server is running.
     */
    public boolean isRunning() {
        return jettyServer.isRunning();
    }

    public Set<Object> getControllers() {
        return controllers;
    }

    public void setControllers(Set<Object> controllers) {
        this.controllers = controllers;
    }
}