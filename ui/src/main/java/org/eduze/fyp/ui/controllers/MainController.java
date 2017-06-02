/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui.controllers;

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
import org.eduze.fyp.core.AnalyticsEngine;
import org.eduze.fyp.core.config.ConfigManager;
import org.eduze.fyp.core.util.Point;
import org.eduze.fyp.core.util.PointMapping;
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
public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final ConfigManager configManager = AnalyticsEngine.getInstance().getConfigManager();

    private Map<Integer, PointMapping> pointMappings = new HashMap<>();
    private Map<Integer, BufferedImage> mapsOnUI = new HashMap<>();

    @FXML
    private Accordion accordion;

    @FXML
    private Button saveConfigButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Map<Integer, BufferedImage> cameraViews = configManager.getCameraViews();

        cameraViews.forEach((cameraId, viewImage) -> {
            mapsOnUI.put(cameraId, configManager.getMap());
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
        });

        saveConfigButton.setOnAction((e) -> {
            long incompleteMappings = pointMappings.values().stream()
                    .filter(pointMapping -> pointMapping.getWorldSpacePoints().size() != 4 || pointMapping.getScreenSpacePoints().size() != 4)
                    .count();

            if (incompleteMappings == 0) {
                pointMappings.forEach(configManager::addPointMapping);
            }
        });
    }

    private void drawPoint(ImageView imageView, BufferedImage image, double x, double y) {
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.red);
        graphics.fillOval((int) x, (int) y, 20, 20);

        Image updatedImage = SwingFXUtils.toFXImage(image, null);
        imageView.setImage(updatedImage);
    }
}
