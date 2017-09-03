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

package org.eduze.fyp.impl;

import org.eduze.fyp.Constants;
import org.eduze.fyp.api.CameraCoordinator;
import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.State;
import org.eduze.fyp.api.StateManager;
import org.eduze.fyp.api.annotations.AutoStart;
import org.eduze.fyp.api.listeners.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@AutoStart(startOrder = 2)
public class CentralizedCameraCoordinator implements CameraCoordinator, ConfigurationListener {

    private static final Logger logger = LoggerFactory.getLogger(CentralizedCameraCoordinator.class);

    private ConfigurationManager configurationManager;
    private Set<InetSocketAddress> cameraIpAndPorts;
    private ExecutorService executorService;
    private StateManager stateManager = new StateManager(State.STOPPED);
    private long currentTimestamp = 1;

    public CentralizedCameraCoordinator(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public void start() {
        stateManager.checkState(State.STOPPED);
        logger.debug("Starting camera coordinator");
        configurationManager.addConfigurationListener(this);
        synchronized (this) {
            cameraIpAndPorts = configurationManager.getCameraIpAndPorts();
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

        executorService.shutdownNow();

        stateManager.setState(State.STOPPED);
        logger.info("Stopped camera coordinator");
    }

    @Override
    public void configurationChanged(ConfigurationManager configurationManager) {
        if (stateManager.isState(State.STARTED)) {
            synchronized (this) {
                cameraIpAndPorts.clear();
                cameraIpAndPorts.addAll(configurationManager.getCameraIpAndPorts());
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
                Thread.sleep(Constants.FRAME_PROCESSING_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting to notify cameras", e);
                break;
            }

            if (!configurationManager.isConfigured()) {
                logger.warn("Configuration manager is not configured yet. Not notifying cameras");
                continue;
            }

            logger.debug("Asking for processed frames for timestamp {}", currentTimestamp);
            byte[] data = String.valueOf(currentTimestamp).getBytes();
            synchronized (this) {
                cameraIpAndPorts.forEach(address -> executorService.submit(() -> notifyCamera(address, data)));
            }

            currentTimestamp++;
        }

        logger.warn("Stopping camera coordination");
    }

    private void notifyCamera(InetSocketAddress address, byte[] timestamp) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http", address.getHostName(), address.getPort(),
                    Constants.CAMERA_COORDINATION_PATH);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", String.valueOf(timestamp.length));
            connection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.write(timestamp);
            outputStream.close();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("Unable to notify {}. Response code {}", address.toString(),
                        connection.getResponseCode());
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error("Error occurred when notifying {}", address.toString(), e);
            } else {
                logger.error("Error occurred when notifying {} with error: {}", address.toString(), e);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public long getCurrentTimestamp() {
        return currentTimestamp;
    }
}
