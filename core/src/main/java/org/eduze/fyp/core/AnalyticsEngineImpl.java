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

import org.eduze.fyp.core.api.AnalyticsEngine;
import org.eduze.fyp.core.api.ConfigurationManager;
import org.eduze.fyp.core.api.MapCollector;
import org.eduze.fyp.core.api.MapProcessor;
import org.eduze.fyp.core.api.annotations.AutoStart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the analytics engine. This will start all the other required components for the analytics engine to run.
 *
 * @author Imesha Sudasingha
 */
@AutoStart
public class AnalyticsEngineImpl extends AnalyticsEngine {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsEngineImpl.class);

    private static AnalyticsEngineImpl instance;

    private ConfigurationManager configurationManager;
    private MapCollector mapCollector;
    private MapProcessor mapProcessor;

    private AnalyticsEngineImpl() {
    }

    @Override
    public void doStart() {
        logger.debug("Starting Analytics Engine");

        logger.info("Analytics Engine started ...");
    }

    @Override
    public void doStop() {
        logger.debug("Stopping Analytics Engine");

        logger.info("Analytics Engine Stopped ...");
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    @Override
    public MapCollector getMapCollector() {
        return mapCollector;
    }

    @Override
    public MapProcessor getMapProcessor() {
        return mapProcessor;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setMapCollector(MapCollector mapCollector) {
        this.mapCollector = mapCollector;
    }

    public void setMapProcessor(MapProcessor mapProcessor) {
        this.mapProcessor = mapProcessor;
    }
}
