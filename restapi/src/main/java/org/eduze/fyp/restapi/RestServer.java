package org.eduze.fyp.restapi;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.eduze.fyp.restapi.controller.config.ConfigController;
import org.eduze.fyp.restapi.controller.realtime.RealTimeController;
import org.eduze.fyp.restapi.util.RequestLogger;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Main class of the <pre>restapi</pre> package. This class is using <pre>Jetty</pre> server to initialize the
 * REST server.
 *
 * @author Imesha Sudasingha
 */
public class RestServer {

    private static final Logger logger = LoggerFactory.getLogger(RestServer.class);

    private static final String JETTY_CONFIG = "jetty.xml";
    private static final String CONTEXT_PATH = "/api/v1/*";

    private Server jettyServer;

    public RestServer() {
        XmlConfiguration configuration;
        try {
            File configFile = new File(JETTY_CONFIG);
            if (!configFile.exists()) {
                logger.error("Config file '{}' is null", JETTY_CONFIG);
                throw new IllegalArgumentException("Jetty configuration could not be found");
            }

            configuration = new XmlConfiguration(configFile.toURI().toURL());
            this.jettyServer = (Server) configuration.configure();
        } catch (Exception e) {
            logger.error("Unable to configure server from configuration file : {}", JETTY_CONFIG);
            throw new IllegalStateException("Unable to configure server", e);
        }

        ResourceConfig config = new ResourceConfig();
        config.register(RealTimeController.class);
        config.register(ConfigController.class);
        // TODO: 5/30/17 Add controllers

        final ServletContainer servletContainer = new ServletContainer(config);
        ServletHolder servlet = new ServletHolder(servletContainer);

        ServletContextHandler context = new ServletContextHandler(jettyServer, null);
        context.addServlet(servlet, CONTEXT_PATH);
        context.setSessionHandler(new SessionHandler());
        jettyServer.setHandler(context);
        jettyServer.setRequestLog(new RequestLogger());
    }

    /**
     * Starts the jetty server. This will also add a shutdown hook to make sure the server will go down properly.
     */
    public void start() {
        try {
            jettyServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    RestServer.this.stop();
                }
            });
        } catch (Exception e) {
            logger.error("Error occurred when starting REST server due to : {}", e.getMessage());
            throw new IllegalStateException("Unable to start REST server", e);
        }
    }

    /**
     * Stops the jetty server
     */
    public void stop() {
        if (jettyServer.isStarted()) {
            try {
                jettyServer.stop();
            } catch (Exception e) {
                logger.error("Error occurred when stopping the REST server due to : {}", e.getMessage());
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

    public static void main(String[] args) {
        RestServer server = new RestServer();

        logger.debug("Starting Reset server");
        server.start();
        logger.debug("Rest server started successfully ...");

        while (server.isRunning()) {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}