package org.eduze.fyp.ui.rest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.eduze.fyp.api.annotations.AutoStart;
import org.eduze.fyp.api.config.Startable;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class of the <pre>rest</pre> package. This class is using <pre>Jetty</pre> server to initialize the
 * REST server.
 *
 * @author Imesha Sudasingha
 */
@AutoStart(startOrder = 2)
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
        //        startRestServer();
        //        if (startWebServer) {
        //            logger.warn("Not starting web server");
        startWebServer();
        //        }
        logger.info("Servers started successfully");
    }

    private void startRestServer() {
        XmlConfiguration configuration;
        try {
            URL configFile = getClass().getClassLoader().getResource(JETTY_CONFIG);
            if (configFile == null) {
                logger.error("Config file '{}' is null", JETTY_CONFIG);
                throw new IllegalArgumentException("Jetty configuration could not be found");
            }

            configuration = new XmlConfiguration(configFile);
            this.jettyServer = (Server) configuration.configure();
        } catch (Exception e) {
            logger.error("Unable to configure server from configuration file : {}", JETTY_CONFIG, e);
            throw new IllegalStateException("Unable to configure server", e);
        }

        ResourceConfig config = new ResourceConfig();
        controllers.forEach(config::register);

        final ServletContainer servletContainer = new ServletContainer(config);
        ServletHolder servlet = new ServletHolder(servletContainer);

        ServletContextHandler context = new ServletContextHandler(jettyServer, null);
        context.addServlet(servlet, CONTEXT_PATH);
        context.setSessionHandler(new SessionHandler());
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

    private void startWebServer() {
        webServer = new Server(8000);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/*");
        URL war = Thread.currentThread().getContextClassLoader().getResource("web");
        File warFile;
        try {
            logger.debug("Using war file {}", war.toString());
            warFile = new File(war.toURI());
        } catch (Exception e) {
            logger.error("Error occurred when determining WAR file", e);
            throw new IllegalStateException("Unable to start WEB Server", e);
        }
        webAppContext.setWar(warFile.getAbsolutePath());
        webAppContext.setExtractWAR(true);
        webServer.setHandler(webAppContext);

        logger.debug("Starting WEB server");
        try {
            webServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(RestServer.this::stop));
        } catch (Exception e) {
            logger.error("Error occurred when starting WEB server due to : {}", e.getMessage());
            throw new IllegalStateException("Unable to start WEB server", e);
        }
        logger.info("WEB Server started successfully ...");
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