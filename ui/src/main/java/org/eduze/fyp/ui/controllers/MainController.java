/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui.controllers;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.eduze.fyp.api.AnalyticsEngine;
import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.MapProcessor;
import org.eduze.fyp.api.listeners.ConfigurationListener;
import org.eduze.fyp.api.listeners.ProcessedDataListener;
import org.eduze.fyp.api.resources.GlobalMap;
import org.eduze.fyp.api.resources.Point;
import org.eduze.fyp.api.resources.PointMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the main window of the {@link org.eduze.fyp.ui.App}
 *
 * @author Imesha Sudasingha
 */
public class MainController implements Initializable, ProcessedDataListener, ConfigurationListener {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private MapProcessor dataProcessor;
    private ConfigurationManager configurationManager;

    private Map<Integer, PointMapping> pointMappings = new HashMap<>();
    private Map<Integer, BufferedImage> mapsOnUI = new HashMap<>();

    private BufferedImage realtimeMap;
    private ImageView realtimeMapImageView;

    @FXML
    private Accordion accordion;

    @FXML
    private Button saveConfigButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AnalyticsEngine analyticsEngine = (AnalyticsEngine) resources.getObject("analyticsEngine");
        configurationManager = analyticsEngine.getConfigurationManager();
        dataProcessor = analyticsEngine.getMapProcessor();

        configurationManager.addConfigurationListener(this);

        realtimeMap = configurationManager.getMap();

        Map<Integer, BufferedImage> cameraViews = configurationManager.getCameraViews();

        cameraViews.forEach(this::checkAndAddCameraToAccordion);

        Image realTimeMap = SwingFXUtils.toFXImage(realtimeMap, null);
        realtimeMapImageView = new ImageView(realTimeMap);

        GridPane gridPane = new GridPane();
        gridPane.add(realtimeMapImageView, 0, 0);
        TitledPane titledPane = new TitledPane("RealTime Map", new Group(gridPane));
        accordion.getPanes().addAll(titledPane);

        saveConfigButton.setOnAction((e) -> {
            long incompleteMappings = pointMappings.values().stream()
                    .filter(pointMapping -> pointMapping.getWorldSpacePoints().size() != 4 || pointMapping.getScreenSpacePoints().size() != 4)
                    .count();

            if (incompleteMappings == 0 && pointMappings.entrySet().size() == configurationManager.getNumberOfCameras()) {
                logger.debug("Adding 2D-3D mapping to configuration : {}", pointMappings);
                pointMappings.forEach(configurationManager::addPointMapping);
            } else {
                logger.debug("Found {} incomplete mappings of {}. Not adding to configuration",
                        incompleteMappings, configurationManager.getNumberOfCameras());
            }
        });

        dataProcessor.addProcessedDataListener(this);
    }

    private synchronized void checkAndAddCameraToAccordion(int cameraId, BufferedImage viewImage) {
        if (mapsOnUI.containsKey(cameraId)) {
            logger.warn("Camera with id: {} already shown in the UI. Ignoring", cameraId);
            return;
        }

        mapsOnUI.put(cameraId, configurationManager.getMap());
        Image mapImage = SwingFXUtils.toFXImage(mapsOnUI.get(cameraId), null);

        Image cameraView = SwingFXUtils.toFXImage(viewImage, null);
        ImageView cameraImageView = new ImageView(cameraView);
        ImageView mapImageView = new ImageView(mapImage);

        cameraImageView.setOnMouseClicked((event) -> {
            logger.debug("CameraView-{} clicked at x : {}, y : {}", cameraId, event.getX(), event.getY());

            PointMapping mapping = pointMappings.computeIfAbsent(cameraId, (k) -> new PointMapping());
            if (mapping.getScreenSpacePoints().size() < 4) {
                mapping.addScreenSpacePoint(new Point(event.getX(), event.getY()));

                drawPoint(cameraImageView, viewImage, event.getX(), event.getY());
            }
        });

        mapImageView.setOnMouseClicked((event) -> {
            logger.debug("Map-{} clicked at x : {}, y : {}", cameraId, event.getX(), event.getY());

            PointMapping mapping = pointMappings.computeIfAbsent(cameraId, (k) -> new PointMapping());
            if (mapping.getWorldSpacePoints().size() < 4) {
                mapping.addWorldSpacePoint(new Point(event.getX(), event.getY()));

                drawPoint(mapImageView, mapsOnUI.get(cameraId), event.getX(), event.getY());
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.add(cameraImageView, 0, 0);
        gridPane.add(mapImageView, 1, 0);

        TitledPane titledPane = new TitledPane("Camera-" + cameraId, new Group(gridPane));
        accordion.getPanes().addAll(titledPane);
    }

    private void drawPoint(ImageView imageView, BufferedImage image, double x, double y) {
        drawPoint(imageView, image, x, y, 10, 10, Color.red);
    }

    private void drawPoint(ImageView imageView, BufferedImage image, double x, double y, int pointWIdth, int pointHeight, Color color) {
        Graphics graphics = image.getGraphics();
        graphics.setColor(color);
        graphics.fillOval((int) x, (int) y, pointWIdth, pointHeight);

        Image updatedImage = SwingFXUtils.toFXImage(image, null);
        Platform.runLater(() -> imageView.setImage(updatedImage));
    }

    @Override
    public void dataProcessed(GlobalMap globalMap) {
        if (realtimeMap == null) return;

        logger.debug("Received {} points for real-time map", globalMap.getPoints().size());
        globalMap.getPoints().forEach(point -> drawPoint(realtimeMapImageView, realtimeMap,
                point.getX(), point.getY(), 5, 5, Color.red));
    }

    @Override
    public synchronized void configurationChanged(ConfigurationManager configurationManager) {
        logger.debug("Configuration change detected. Updating UI");
        Platform.runLater(() -> configurationManager.getCameraViews().forEach(this::checkAndAddCameraToAccordion));
    }
}
