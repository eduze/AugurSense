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

import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.core.api.DataCollector;
import org.eduze.fyp.core.api.listeners.DataListener;
import org.eduze.fyp.core.api.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryDataCollector implements DataCollector {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryDataCollector.class);

    /** Timestamps within this range are taken together for processing. Ex: 15235000-15236000 taken together */
    private int timestampThreshold = 1000;
    private Lock lock = new ReentrantLock();

    private Map<Long, List<Point>> pointsMap = new HashMap<>();
    private Map<Long, Integer> cameraCountMap = new HashMap<>();
    private Set<DataListener> dataListeners = new HashSet<>();
    private Set<Long> processedTimestamps = new HashSet<>();

    private int cameraCount;
    private boolean startProcessing;

    public InMemoryDataCollector(int cameraCount) {
        this.startProcessing = cameraCount > 0;
        this.cameraCount = cameraCount;
    }

    @Override
    public synchronized void addPoints(long timestamp, List<Point> coordinates) {
        // Using integer division here
        long key = timestamp / timestampThreshold * timestampThreshold;

        if (!startProcessing || processedTimestamps.contains(key)) {
            return;
        }

        List<Point> pointList = pointsMap.computeIfAbsent(key, k -> new ArrayList<>());
        pointList.addAll(coordinates);

        if (cameraCountMap.containsKey(key)) {
            cameraCountMap.put(key, cameraCountMap.get(key) + 1);
        } else {
            cameraCountMap.put(key, 1);
        }

        if (startProcessing && cameraCountMap.get(key) == cameraCount) {
            logger.debug("Broadcasting data for timestamp : {}", key);
            dataListeners.forEach(listener -> listener.dataReceived(pointList));
            processedTimestamps.add(key);
            cameraCountMap.remove(key);
            pointsMap.remove(key);
        }
    }

    @Override
    public void configurationChanged(ConfigurationManager configurationManager) {
        cameraCount = configurationManager.getNumberOfCameras();
        startProcessing = cameraCount > 0;
    }

    @Override
    public synchronized void addDataListener(DataListener listener) {
        dataListeners.add(listener);
    }

    @Override
    public synchronized void removeDataListener(DataListener listener) {
        dataListeners.remove(listener);
    }
}
