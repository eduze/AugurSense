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

package org.eduze.fyp.core;

import org.eduze.fyp.api.CameraCoordinator;
import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.MapProcessor;
import org.eduze.fyp.api.State;
import org.eduze.fyp.api.StateManager;
import org.eduze.fyp.api.annotations.AutoStart;
import org.eduze.fyp.api.listeners.ConfigurationListener;
import org.eduze.fyp.api.resources.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import static org.eduze.fyp.api.Constants.FRAME_PROCESSING_INTERVAL;

@AutoStart(startOrder = 2)
public class CentralizedCameraCoordinator implements CameraCoordinator, ConfigurationListener {

    private static final Logger logger = LoggerFactory.getLogger(CentralizedCameraCoordinator.class);

    private ConfigurationManager configurationManager;
    private Map<InetSocketAddress, CameraNotifier> cameraNotifiers = new HashMap<>();
    private ExecutorService executorService;
    private StateManager stateManager = new StateManager(State.STOPPED);
    private long currentTimestamp = 0;
    private long realTimestamp = 0;
    /** Whether to use timestamps as per current clock time or as a reference time starting from 0 */
    private boolean useClock = false;
    private long intervalMs;
    private MapProcessor mapProcessor;

    public CentralizedCameraCoordinator(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public void start() {
        stateManager.checkState(State.STOPPED);

        if (intervalMs == 0 || intervalMs < 0) {
            throw new IllegalArgumentException("IntervalMs is not set");
        }

        logger.debug("Starting camera coordinator");
        configurationManager.addConfigurationListener(this);
        synchronized (this) {
            configurationManager.getCameraIpAndPorts()
                    .forEach(address ->
                            cameraNotifiers.putIfAbsent(address, new CameraNotifier(address, this)));
        }

        executorService = new ForkJoinPool();
        executorService.submit(this::startCoordination);

        stateManager.setState(State.STARTED);
        logger.info("Started camera coordinator");
    }

    @Override
    public void stop() {
        stateManager.checkState(State.STARTED, State.STOPPING);
        stateManager.setState(State.STOPPING);
        logger.debug("Stopping camera coordinator");

        clearNotifiers();
        executorService.shutdownNow();

        stateManager.setState(State.STOPPED);
        logger.info("Stopped camera coordinator");
    }

    @Override
    public void configurationChanged(ConfigurationManager configurationManager) {
        if (stateManager.isState(State.STARTED)) {
            synchronized (this) {
                clearNotifiers();
                configurationManager.getCameraIpAndPorts()
                        .forEach(address ->
                                cameraNotifiers.putIfAbsent(address, new CameraNotifier(address, this)));
            }
        }
    }

    private void startCoordination() {
        try {
            logger.debug("Waiting for state {}", State.STARTED);
            stateManager.waitFor(State.STARTED);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for state: {}", State.STARTED, e);
        }
        logger.debug("Starting camera coordination");

        while (stateManager.isState(State.STARTED)) {
            try {
                Thread.sleep(FRAME_PROCESSING_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting to notify cameras", e);
                break;
            }

            if (!configurationManager.isConfigured()) {
                logger.warn("Configuration manager is not configured yet. Not notifying cameras");
                continue;
            }

            mapProcessor.nextFrame(new Date(currentTimestamp));

            logger.debug("Asking for processed frames for timestamp {}", currentTimestamp);
            synchronized (this) {
                // TODO: 10/3/17 Is there an error? We take timestamp few milli-seconds earlier
                realTimestamp = new Date().getTime();
                if (useClock) {
                    currentTimestamp = realTimestamp;
                } else {
                    currentTimestamp += intervalMs;
                }

                cameraNotifiers.values()
                        .forEach(notifier -> {
                            executorService.submit(() -> {
                                notifier.notifyCamera(currentTimestamp);
                            });
                        });
            }
        }

        logger.warn("Stopping camera coordination");
    }


    private synchronized void clearNotifiers() {
        cameraNotifiers.values().forEach(CameraNotifier::stop);
        cameraNotifiers.clear();
    }

    public void addLocalMap(LocalMap map) {
        map.setTimestamp(realTimestamp);
        map.getPersonCoordinates()
                .forEach(personCoordinate -> personCoordinate.setTimestamp(realTimestamp));

        mapProcessor.addLocalMap(map);
    }

    @Override
    public long getCurrentTimestamp() {
        return currentTimestamp;
    }

    public MapProcessor getMapProcessor() {
        return mapProcessor;
    }

    public void setMapProcessor(MapProcessor mapProcessor) {
        this.mapProcessor = mapProcessor;
    }

    public boolean isUseClock() {
        return useClock;
    }

    public void setUseClock(boolean useClock) {
        this.useClock = useClock;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }
}
