/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.eduze.fyp.core.api.AnalyticsEngineFactory;
import org.eduze.fyp.core.api.AnalyticsEngine;
import org.eduze.fyp.restapi.RestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final String MAIN_FXML = "/fxml/main.fxml";

    private final AnalyticsEngine analyticsEngine = AnalyticsEngineFactory.getAnalyticsEngine();
    private final RestServer restServer = RestServer.getInstance();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.debug("Starting Application");
        restServer.start();
        analyticsEngine.start();

        Parent root = FXMLLoader.load(getClass().getResource(MAIN_FXML));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Analytics Engine");
        primaryStage.setScene(scene);
        primaryStage.show();

        logger.info("Application started successfully");
    }

    public void stop() throws Exception {
        super.stop();

        restServer.stop();
        analyticsEngine.stop();

        logger.info("Application stopped ...");
    }
}
