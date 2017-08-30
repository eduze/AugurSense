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

import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.MapCollectionStrategy;
import org.eduze.fyp.api.MapCollector;
import org.eduze.fyp.api.annotations.AutoStart;
import org.eduze.fyp.api.listeners.MapListener;
import org.eduze.fyp.api.resources.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@AutoStart(startOrder = 1)
public class InMemoryMapCollector implements MapCollector {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryMapCollector.class);

    private MapCollectionStrategy mapCollectionStrategy;
    private Set<MapListener> mapListeners = new HashSet<>();
    private int cameraCount;

    public InMemoryMapCollector() {
        cameraCount = 0;
    }

    @Override
    public synchronized void addPoints(LocalMap map) {
        logger.debug("Adding a local map for camera : {}", map.getCameraId());
        mapCollectionStrategy.submit(map);
    }

    @Override
    public void publishMaps(Set<LocalMap> maps) {
        logger.debug("Publishing {} local maps", maps.size());
        mapListeners.forEach(mapListener -> mapListener.dataReceived(maps));
    }

    @Override
    public void configurationChanged(ConfigurationManager configurationManager) {
        cameraCount = configurationManager.getNumberOfCameras();
    }

    @Override
    public synchronized void addMapListener(MapListener listener) {
        mapListeners.add(listener);
        logger.info("Added map listener (Total map listeners: {})", mapListeners.size());
    }

    @Override
    public synchronized void removeMapListener(MapListener listener) {
        mapListeners.remove(listener);
        logger.info("Removed map listener (Total map listeners: {})", mapListeners.size());
    }

    public MapCollectionStrategy getMapCollectionStrategy() {
        return mapCollectionStrategy;
    }

    public void setMapCollectionStrategy(MapCollectionStrategy mapCollectionStrategy) {
        this.mapCollectionStrategy = mapCollectionStrategy;
    }

    @Override
    public void start() {
        logger.debug("Starting map collector");

        logger.info("Map collector started");
    }

    @Override
    public void stop() {
        logger.debug("Stopping map collector");

        logger.info("Map collector stopped");
    }

    public void setMapListeners(Set<MapListener> mapListeners) {
        logger.info("Setting map listeners (Total : {})", mapListeners.size());
        this.mapListeners = mapListeners;
    }
}
