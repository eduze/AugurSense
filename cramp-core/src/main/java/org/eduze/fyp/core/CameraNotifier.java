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

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.eduze.fyp.api.resources.LocalMap;
import org.eduze.fyp.api.util.Args;
import org.eduze.fyp.core.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import static org.eduze.fyp.api.Constants.CAMERA_NOTIFICATION_PATH_PATTERN;
import static org.eduze.fyp.api.Constants.CAMERA_NOTIFY_TIMEOUT;

/**
 * Class responsible for notifying and retrieving processed local maps corresponding to a timestamp.
 *
 * @author Imesha Sudasingha
 */
public class CameraNotifier implements FutureCallback<HttpResponse> {

    private final Logger logger;
    private final CloseableHttpAsyncClient client;
    private State state = State.IDLE;
    private long currentTimestamp;
    private final InetSocketAddress ipAndPort;
    private CentralizedCameraCoordinator cameraCoordinator;

    public CameraNotifier(InetSocketAddress ipAndPort, CentralizedCameraCoordinator cameraCoordinator) {
        Args.notNull(ipAndPort, "ipAndPort");
        Args.notNull(cameraCoordinator, "cameraCoordinator");

        this.logger = LoggerFactory.getLogger(CameraNotifier.class.getSimpleName() + ":" + ipAndPort.toString());
        this.cameraCoordinator = cameraCoordinator;
        this.ipAndPort = ipAndPort;

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CAMERA_NOTIFY_TIMEOUT)
                .setConnectionRequestTimeout(CAMERA_NOTIFY_TIMEOUT)
                .setSocketTimeout(CAMERA_NOTIFY_TIMEOUT)
                .build();
        this.client = HttpAsyncClients.custom()
                .setDefaultRequestConfig(config)
                .build();
        client.start();
    }

    public void notifyCamera(long timestamp) {
        if (!State.IDLE.equals(state)) {
            logger.warn("Notifier is not ready to notify {} to camera", timestamp);
            return;
        }

        setState(State.OCCUPIED);
        currentTimestamp = timestamp;
        try {
            logger.debug("Sending notification for timestamp {}", currentTimestamp);
            URI endpoint = new URIBuilder()
                    .setScheme("http")
                    .setHost(ipAndPort.getHostName())
                    .setPort(ipAndPort.getPort())
                    .setPath(String.format(CAMERA_NOTIFICATION_PATH_PATTERN, timestamp))
                    .build();

            HttpGet request = new HttpGet(endpoint);
            logger.debug("Sending notification to {}", request.getRequestLine());
            client.execute(request, this);
        } catch (URISyntaxException e) {
            logger.error("Error occurred when creating URI to notify", e);
            setState(State.IDLE);
        } catch (Exception e) {
            logger.error("Error occurred", e);
            setState(State.IDLE);
        }
    }

    @Override
    public void completed(HttpResponse httpResponse) {
        try {
            String entity = HttpUtils.readEntity(httpResponse.getEntity());
            LocalMap receivedMap = HttpUtils.mapEntity(entity, LocalMap.class);
            cameraCoordinator.addLocalMap(receivedMap);
            setState(State.IDLE);
        } catch (IOException e) {
            logger.error("Error occurred when reading entity", e);
            setState(State.IDLE);
        }
    }

    @Override
    public void failed(Exception e) {
        logger.error("Error occurred when notifying camera with", e);
        setState(State.IDLE);
    }

    @Override
    public void cancelled() {
        logger.warn("Request sending cancelled");
        setState(State.IDLE);
    }

    public InetSocketAddress getIpAndPort() {
        return ipAndPort;
    }

    private void setState(State state) {
        synchronized (this) {
            this.state = state;
        }
    }

    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            logger.error("Error occurred when closing client");
        }
        setState(State.STOPPED);
        logger.info("Notifier stopped ...");
    }

    private enum State {
        IDLE,
        OCCUPIED,
        STOPPED
    }
}
