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
import org.eduze.fyp.api.listeners.ConfigurationListener;
import org.eduze.fyp.api.listeners.ProcessedMapListener;
import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.api.resources.Point;
import org.eduze.fyp.api.resources.PointMapping;
import org.eduze.fyp.ui.controllers.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the main window of the {@link org.eduze.fyp.ui.App}
 *
 * @author Imesha Sudasingha
 */
public class MainController implements Initializable, ProcessedMapListener, ConfigurationListener {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private AnalyticsEngine analyticsEngine;

    private Map<Integer, PointMapping> pointMappings = new HashMap<>();
    private Map<Integer, BufferedImage> mapsOnUI = new HashMap<>();

    private BufferedImage realtimeMap;
    private BufferedImage originalMap;
    private ImageView realtimeMapImageView;

    @FXML
    private Accordion accordion;

    @FXML
    private Button saveConfigButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        analyticsEngine = (AnalyticsEngine) resources.getObject("analyticsEngine");

        ConfigurationManager configurationManager = analyticsEngine.getConfigurationManager();
        configurationManager.addConfigurationListener(this);
        realtimeMap = ImageUtils.copyImage(configurationManager.getMap());
        originalMap = ImageUtils.copyImage(configurationManager.getMap());

        Map<Integer, BufferedImage> cameraViews = configurationManager.getCameraViews();

        cameraViews.forEach((k,v)->{
            if(configurationManager.getInitialMappings().containsKey(k))
            {
                this.checkAndAddCameraToAccordion(k,v,configurationManager.getInitialMappings().get(k));
            }
            else{
                this.checkAndAddCameraToAccordion(k,v,null);
            }
        });

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
                logger.info("Adding 2D-3D mapping to configuration : {}", pointMappings);
                pointMappings.forEach(configurationManager::addPointMapping);
            } else {
                logger.debug("Found {} incomplete mappings of {}. Not adding to configuration",
                        incompleteMappings, configurationManager.getNumberOfCameras());
            }
        });

        analyticsEngine.getMapProcessor().addProcessedMapListener(this);
    }

    private synchronized void checkAndAddCameraToAccordion(int cameraId, BufferedImage viewImage, PointMapping initialMapping) {
        if (mapsOnUI.containsKey(cameraId)) {
            logger.warn("Camera with id: {} already shown in the UI. Ignoring", cameraId);
            return;
        }

        mapsOnUI.put(cameraId, analyticsEngine.getConfigurationManager().getMap());
        Image mapImage = SwingFXUtils.toFXImage(mapsOnUI.get(cameraId), null);

        Image cameraView = SwingFXUtils.toFXImage(viewImage, null);
        ImageView cameraImageView = new ImageView(cameraView);
        ImageView mapImageView = new ImageView(mapImage);

        if(initialMapping != null)
        {
            for(Point p : initialMapping.getScreenSpacePoints()){
                PointMapping mapping = pointMappings.computeIfAbsent(cameraId, (k) -> new PointMapping());
                if (mapping.getScreenSpacePoints().size() < 4) {
                    mapping.addScreenSpacePoint(new Point(p.getX(), p.getY()));

                    drawPoint(cameraImageView, viewImage, p.getX(), p.getY());
                }
            }
            for(Point p : initialMapping.getWorldSpacePoints()){
                PointMapping mapping = pointMappings.computeIfAbsent(cameraId, (k) -> new PointMapping());
                if (mapping.getWorldSpacePoints().size() < 4) {
                    mapping.addWorldSpacePoint(new Point(p.getX(), p.getY()));

                    drawPoint(mapImageView, mapsOnUI.get(cameraId), p.getX(), p.getY());
                }
            }
        }

        cameraImageView.setOnMouseClicked((event) -> {
            logger.debug("CameraConfig-{} clicked at x : {}, y : {}", cameraId, event.getX(), event.getY());

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

    private void drawPoint(ImageView imageView, BufferedImage image, double x, double y, int pointWidth, int pointHeight, Color color) {
        Graphics graphics = image.getGraphics();
        graphics.setColor(color);
        graphics.fillOval((int) x, (int) y, pointWidth, pointHeight);

        Image updatedImage = SwingFXUtils.toFXImage(image, null);
        Platform.runLater(() -> imageView.setImage(updatedImage));
    }

    private void drawPath(ImageView imageView, BufferedImage image, List<Point> points, int pointWidth, Color color) {
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(pointWidth));

        Point p = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            graphics.drawLine((int) p.getX(), (int) p.getY(), (int) points.get(i).getX(), (int) points.get(i).getY());
            p = points.get(i);
        }

        Image updatedImage = SwingFXUtils.toFXImage(image, null);
        Platform.runLater(() -> imageView.setImage(updatedImage));
    }

    @Override
    public void mapProcessed(List<List<PersonSnapshot>> snapshots) {
        if (realtimeMap == null || snapshots.size() == 0) return;

        BufferedImage map = ImageUtils.copyImage(originalMap);

        logger.debug("Received {} points for real-time map", snapshots.size());
        snapshots.forEach(snapshotList -> {
            List<Point> points = snapshotList.stream()
                    .map(snapshot -> new Point(snapshot.getX(), snapshot.getY()))
                    .collect(Collectors.toList());
            if (points.size() == 1) {
                drawPoint(realtimeMapImageView, map, points.get(0).getX(), points.get(0).getY());
            } else if (points.size() > 1) {
                drawPath(realtimeMapImageView, map, points, 5, Color.red);
            }
        });
    }

    @Override
    public void onFrame(List<List<PersonSnapshot>> snapshots, Date timestamp) {

    }

    @Override
    public synchronized void configurationChanged(ConfigurationManager configurationManager) {
        logger.debug("Configuration change detected. Updating UI");
        Platform.runLater(() -> configurationManager.getCameraViews().forEach((k,v)->{
            if(configurationManager.getInitialMappings().containsKey(k))
            {
                this.checkAndAddCameraToAccordion(k,v,configurationManager.getInitialMappings().get(k));
            }
            else{
                this.checkAndAddCameraToAccordion(k,v,null);
            }
        }));

    }
}
