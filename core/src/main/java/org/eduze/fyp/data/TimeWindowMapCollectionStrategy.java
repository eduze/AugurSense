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

package org.eduze.fyp.data;

import org.eduze.fyp.api.MapCollectionStrategy;
import org.eduze.fyp.api.MapCollector;
import org.eduze.fyp.api.annotations.AutoStart;
import org.eduze.fyp.api.resources.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Strategy class to publish maps based on the constant time window approach
 *
 * @author Imesha Sudasingha
 */
@AutoStart(startOrder = 2)
public class TimeWindowMapCollectionStrategy implements MapCollectionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(TimeWindowMapCollectionStrategy.class);

    private ExecutorService executorService;
    private Lock lock = new ReentrantLock();
    private Map<Integer, LocalMap> currentWindow = new HashMap<>();

    private MapCollector mapCollector;
    private int timeWindowMs = 5000;

    public TimeWindowMapCollectionStrategy(MapCollector mapCollector) {
        this.mapCollector = mapCollector;
    }

    @Override
    public void submit(LocalMap map) {
        if (executorService.isShutdown()) {
            logger.warn("Map collection has stopped. Ignoring received frames");
            return;
        }

        logger.debug("Received a local map for camera : {}", map.getCameraId(), map.getTimestamp());
        lock.lock();
        try {
            LocalMap localMap = currentWindow.get(map.getCameraId());
            if (localMap == null || localMap.getTimestamp() < map.getTimestamp()) {
                logger.debug("Adding local map-{} to time window", map);
                currentWindow.put(map.getCameraId(), map);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void start() {
        logger.debug("Starting map collector ...");
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::doProcessing);
    }

    private void doProcessing() {
        while (true) {
            try {
                Thread.sleep(timeWindowMs);
            } catch (InterruptedException e) {
                logger.error("Processing thread interrupted. Shutting down", e);
                break;
            }

            Set<LocalMap> localMaps;
            lock.lock();
            try {
                logger.debug("Publishing time window (Total maps for window: {})", currentWindow.size());
                localMaps = new HashSet<>(currentWindow.values());
                currentWindow.clear();
            } finally {
                lock.unlock();
            }

            mapCollector.publishMaps(localMaps);
        }
    }

    @Override
    public void stop() {
        logger.debug("Stopping map collector ...");
        executorService.shutdownNow();
    }

    public int getTimeWindowMs() {
        return timeWindowMs;
    }

    public void setTimeWindowMs(int timeWindowMs) {
        this.timeWindowMs = timeWindowMs;
    }
}
