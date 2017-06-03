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

import org.eduze.fyp.core.api.DataProcessor;
import org.eduze.fyp.core.api.Point;
import org.eduze.fyp.core.api.listeners.ProcessedDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InMemoryDataProcessor implements DataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryDataProcessor.class);

    private Set<ProcessedDataListener> processedDataListeners = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void dataReceived(List<Point> points) {
        logger.debug("Received {} points for processing", points.size());

        processedDataListeners.forEach(listener -> listener.dataProcessed(points));
    }

    @Override
    public void addProcessedDataListener(ProcessedDataListener listener) {
        processedDataListeners.add(listener);
    }

    @Override
    public void removeProcessedDataListener(ProcessedDataListener listener) {
        processedDataListeners.remove(listener);
    }
}
