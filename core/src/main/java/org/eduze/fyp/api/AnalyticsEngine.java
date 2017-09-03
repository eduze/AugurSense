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
package org.eduze.fyp.api;

import org.eduze.fyp.api.config.Startable;
import org.eduze.fyp.api.util.Args;

/**
 * Interface for Analytics Engine core
 *
 * @author Imesha Sudasingha
 */
public abstract class AnalyticsEngine implements Startable {

    private ConfigurationManager configurationManager;
    private MapProcessor mapProcessor;
    private CameraCoordinator cameraCoordinator;

    public void start() {
        Args.notNull(configurationManager, "configurationManager");
        Args.notNull(mapProcessor, "mapProcessor");
        Args.notNull(cameraCoordinator, "cameraCoordinator");
        doStart();
    }

    public void stop() {
        doStop();
    }

    protected abstract void doStart();

    protected abstract void doStop();

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public MapProcessor getMapProcessor() {
        return mapProcessor;
    }

    public CameraCoordinator getCameraCoordinator() {
        return cameraCoordinator;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setMapProcessor(MapProcessor mapProcessor) {
        this.mapProcessor = mapProcessor;
    }

    public void setCameraCoordinator(CameraCoordinator cameraCoordinator) {
        this.cameraCoordinator = cameraCoordinator;
    }
}
