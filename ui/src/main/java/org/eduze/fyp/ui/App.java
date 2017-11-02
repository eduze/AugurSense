/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.eduze.fyp.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.eduze.fyp.CHASS;
import org.eduze.fyp.api.AnalyticsEngine;
import org.eduze.fyp.ui.resources.AppResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI Application which does the initial configuration.
 *
 * @author Imesha Sudasingha
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final String MAIN_FXML = "/fxml/main.fxml";

    private static App instance;
    private CHASS chass;

    public App() {
        chass = CHASS.getInstance();
        App.instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.debug("Starting {}", CHASS.class);
        chass.start();

        AnalyticsEngine analyticsEngine = chass.getApplicationContext().getBean(AnalyticsEngine.class);

        logger.debug("Starting UI Application ...");
        AppResources resources = new AppResources();
        resources.addResource("analyticsEngine", analyticsEngine);
        Parent root = FXMLLoader.load(getClass().getResource(MAIN_FXML), resources);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Analytics Engine");
        primaryStage.setScene(scene);
        primaryStage.show();

        logger.info("UI Application started");
    }

    @Override
    public void stop() throws Exception {
        try {
            super.stop();
        } catch (Exception e) {
            logger.error("Error occurred when stopping application");
        }

        chass.stop();
        logger.info("Application stopped ...");
    }

    public CHASS getChass() {
        return chass;
    }

    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        App.launch(args);
    }
}
