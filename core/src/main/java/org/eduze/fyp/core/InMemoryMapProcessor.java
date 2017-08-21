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

import org.eduze.fyp.api.MapProcessor;
import org.eduze.fyp.api.annotations.AutoStart;
import org.eduze.fyp.api.listeners.ProcessedDataListener;
import org.eduze.fyp.api.resources.GlobalMap;
import org.eduze.fyp.api.resources.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@AutoStart(startOrder = 1)
public class InMemoryMapProcessor implements MapProcessor {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryMapProcessor.class);

    private Set<ProcessedDataListener> processedDataListeners = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void dataReceived(Set<LocalMap> maps) {
        logger.debug("Received {} points for processing", maps.size());

        // TODO: 8/21/17 Process the points and generate global map
        GlobalMap map = new GlobalMap();
        maps.forEach(localMap -> map.getPoints().addAll(localMap.getPoints()));

        processedDataListeners.forEach(listener -> listener.dataProcessed(map));
    }

    @Override
    public void addProcessedDataListener(ProcessedDataListener listener) {
        processedDataListeners.add(listener);
    }

    @Override
    public void removeProcessedDataListener(ProcessedDataListener listener) {
        processedDataListeners.remove(listener);
    }

    @Override
    public void start() {
        logger.debug("Starting map processor");

        logger.info("Map processor started");
    }

    @Override
    public void stop() {
        logger.debug("Stopping map processor");

        logger.info("Map processor stopped");
    }

    public void setProcessedDataListeners(Set<ProcessedDataListener> processedDataListeners) {
        this.processedDataListeners.addAll(processedDataListeners);
    }
}
