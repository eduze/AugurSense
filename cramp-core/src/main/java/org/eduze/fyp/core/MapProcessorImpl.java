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

package org.eduze.fyp.core;

import org.eduze.fyp.api.CameraCoordinator;
import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.MapProcessor;
import org.eduze.fyp.api.State;
import org.eduze.fyp.api.StateManager;
import org.eduze.fyp.api.annotations.AutoStart;
import org.eduze.fyp.api.listeners.ProcessedMapListener;
import org.eduze.fyp.api.model.CameraConfig;
import org.eduze.fyp.api.model.CameraGroup;
import org.eduze.fyp.api.resources.LocalMap;
import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.api.util.Args;
import org.eduze.fyp.core.resources.GlobalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.eduze.fyp.api.Constants.MAP_REFRESH_INTERVAL;
import static org.eduze.fyp.api.Constants.MAP_REFRESH_THRESHOLD;

/**
 * Class responsible for processing local maps sent by multiple cameras and aggregating for global maps.
 * TODO address configuration changes
 *
 * @author Imesha Sudasinha
 * @author Madhawa Vidanapathirana
 */
@AutoStart(startOrder = 1)
public class MapProcessorImpl implements MapProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MapProcessorImpl.class);

    private final Map<Integer, GlobalMap> globalMaps = new HashMap<>();
    private final StateManager stateManager = new StateManager(State.STOPPED);
    private final Queue<LocalMap> localMapQueue = new LinkedList<>();
    private CameraCoordinator cameraCoordinator;
    private Set<ProcessedMapListener> mapListeners = new HashSet<>();
    private ExecutorService processor;

    private ConfigurationManager configurationManager;

    private int zonePersistentThreshold;
    private int zonePersistentScanCount;

    @Override
    public void addLocalMap(LocalMap map) {
        stateManager.checkState(State.STARTED);
        logger.debug("Adding a local map for camera : {}", map.getCameraId());

        // Point mapping conversion
        //        CameraConfig cameraConfig = configurationManager.getCameraConfig(map.getCameraId());
        //        map.getPersonCoordinates().forEach(personCoordinate -> {
        //            personCoordinate.setX(personCoordinate.getX() * Constants.CAMERA_VIEW_WIDTH / cameraConfig.getWidth());
        //            personCoordinate.setY(personCoordinate.getY() * Constants.CAMERA_VIEW_HEIGHT / cameraConfig.getHeight());
        //        });

        synchronized (this) {
            localMapQueue.add(map);
        }
    }

    @Override
    public synchronized void addProcessedMapListener(ProcessedMapListener listener) {
        mapListeners.add(listener);
    }

    @Override
    public void removeProcessedMapListener(ProcessedMapListener listener) {
        mapListeners.remove(listener);
    }

    @Override
    public void start() {
        Args.notNull(cameraCoordinator, "cameraCoordinator");

        configurationManager.getCameraGroups()
                .forEach((id, cameraGroup) -> {
                    GlobalMap globalMap = new GlobalMap();

                    ZoneMapper zoneMapper = new ZoneMapper(cameraGroup.getZones(),
                            zonePersistentScanCount, zonePersistentThreshold);
                    globalMap.setZoneMapper(zoneMapper);
                    //                    globalMap.setPhotoMapper(photoMapper);
                    //                    globalMap.setAccuracyTester(accuracyTester);
                    globalMaps.put(id, globalMap);
                });

        stateManager.checkState(State.STOPPED);
        logger.debug("Starting map collector");

        processor = Executors.newSingleThreadExecutor();
        processor.submit((Runnable) () -> {
            try {
                stateManager.waitFor(State.STARTED);
            } catch (InterruptedException e) {
                logger.error("Map processing thread interrupted while waiting for start", e);
            }
            logger.debug("Starting map processing");

            // TODO: 9/3/17 Optimize to use a thread pool of 2 at least to focus more on processing
            long lastTimestamp = 0;
            for (; ; ) {
                if (!stateManager.isState(State.STARTED)) {
                    break;
                }

                LocalMap nextMap;
                synchronized (this) {
                    nextMap = localMapQueue.poll();
                }

                try {
                    GlobalMap globalMap = null;
                    if (nextMap != null) {
                        CameraConfig cameraConfig = configurationManager.getCameraConfig(nextMap.getCameraId());
                        CameraGroup cameraGroup = cameraConfig.getCameraGroup();
                        globalMap = globalMaps.get(cameraGroup.getId());
                        globalMap.update(nextMap);

                        List<List<PersonSnapshot>> snapshots = globalMap.getSnapshot();
                        synchronized (this) {
                            mapListeners.forEach(listener -> listener.mapProcessed(cameraGroup, snapshots));
                        }
                    }

                    if (cameraCoordinator.getRealTimestamp() - lastTimestamp > MAP_REFRESH_INTERVAL) {
                        lastTimestamp = cameraCoordinator.getRealTimestamp();
                        long minTimestamp = lastTimestamp - MAP_REFRESH_THRESHOLD;
                        globalMaps.values().forEach(map -> map.refresh(minTimestamp));
                    }
                } catch (Exception e) {
                    logger.error("Error occurred in map processing", e);
                }
            }
        });


        stateManager.setState(State.STARTED);
        logger.info("Map collector started");
    }

    @Override
    public void stop() {
        stateManager.checkState(State.STARTED);
        logger.debug("Stopping map collector");

        processor.shutdownNow();

        logger.info("Map collector stopped");
        stateManager.setState(State.STOPPED);
    }

    public synchronized void setMapListeners(Set<ProcessedMapListener> listeners) {
        if (listeners != null) {
            mapListeners.addAll(listeners);
        }
    }

    public void setCameraCoordinator(CameraCoordinator cameraCoordinator) {
        this.cameraCoordinator = cameraCoordinator;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setZonePersistentThreshold(int zonePersistentThreshold) {this.zonePersistentThreshold = zonePersistentThreshold;}

    public int getZonePersistentThreshold() { return zonePersistentThreshold; }

    public void setZonePersistentScanCount(int zonePersistentScanCount) {this.zonePersistentScanCount = zonePersistentScanCount;}

    public int getZonePersistentScanCount() { return zonePersistentScanCount; }
}
