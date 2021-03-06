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
package org.augur.sense.core;

import org.augur.sense.api.ConfigurationManager;
import org.augur.sense.api.Constants;
import org.augur.sense.api.State;
import org.augur.sense.api.StateManager;
import org.augur.sense.api.model.CameraConfig;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.PointMapping;
import org.augur.sense.api.model.Zone;
import org.augur.sense.core.db.dao.CameraConfigDAO;
import org.augur.sense.core.db.dao.ZoneDAO;
import org.augur.sense.core.db.dao.CameraConfigDAO;
import org.augur.sense.core.db.dao.ZoneDAO;
import org.augur.sense.api.ConfigurationManager;
import org.augur.sense.api.State;
import org.augur.sense.api.StateManager;
import org.augur.sense.api.annotations.AutoStart;
import org.augur.sense.api.annotations.Mode;
import org.augur.sense.api.listeners.ConfigurationListener;
import org.augur.sense.api.model.CameraConfig;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.PointMapping;
import org.augur.sense.api.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.augur.sense.api.Constants.Properties.FLOOR_MAP_IMAGE;
import static org.augur.sense.api.Constants.ZONE_NAME_WORLD;

/**
 * {@link ConfigurationManager} implementation which is doing all operations using in-memory data storage.
 *
 * @author Imesha Sudasingha
 */
@AutoStart(startOrder = 1, mode = Mode.PASSIVE)
public class ConfigurationManagerImpl implements ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagerImpl.class);

    private final StateManager stateManager = new StateManager(State.STOPPED);

    private String propertiesFile;
    private BufferedImage map;

    private Set<ConfigurationListener> configurationListeners = new HashSet<>();
    private Map<Integer, CameraConfig> cameraConfigs = new ConcurrentHashMap<>();
    private Map<Integer, CameraGroup> cameraGroups = new ConcurrentHashMap<>();
    // TODO: 1/5/18 If zones are deleted through UI, it is not reflected here

    private List<Zone> zones = new ArrayList<>();
    private CameraConfigDAO cameraConfigDAO;
    private ZoneDAO zoneDAO;

    public ConfigurationManagerImpl() { }

    @Override
    public void start() {
        stateManager.checkState(State.STOPPED);

        if (propertiesFile == null) {
            throw new IllegalArgumentException("No properties file is given");
        }

        setup();
        try {
            loadConfiguration();
        } catch (IOException e) {
            logger.error("Error occurred when loading configuration", e);
            throw new IllegalArgumentException("Unable to load properties", e);
        }

        String mapPath = System.getProperty(Constants.Properties.FLOOR_MAP_IMAGE);
        if (mapPath == null) {
            throw new IllegalArgumentException("No Map File Path given");
        }
        logger.debug("Using map image : {}", mapPath);

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(mapPath)) {
            map = ImageIO.read(in);
        } catch (IOException e) {
            logger.error("Unable to load the map image : {}", mapPath, e);
            throw new IllegalArgumentException("Unable to load the map image");
        }

        stateManager.setState(State.STARTED);
        notifyConfigurationChange();
    }

    private void setup() {
        cameraConfigDAO.cameraGroups().forEach(cameraGroup -> {
            List<Zone> zones = cameraGroup.getZones();
            if (zones.stream().noneMatch(zone -> Constants.ZONE_NAME_WORLD.equals(zone.getZoneName()))) {
                Zone world = new Zone(Constants.ZONE_NAME_WORLD, Integer.MAX_VALUE, null, null, cameraGroup);
                logger.debug("Adding zone: [{}] to camera group: [{}]", world, cameraGroup);
                zoneDAO.save(world);
            }
        });
    }

    private void loadConfiguration() throws IOException {
        logger.info("Loading configurations");

        try (InputStream propertiesFile = new FileInputStream(this.propertiesFile)) {
            System.getProperties().load(propertiesFile);
        }

        this.zones.clear();
        this.zones.addAll(zoneDAO.list());

        this.cameraConfigDAO.list()
                .forEach(cameraConfig -> this.cameraConfigs.put(cameraConfig.getCameraId(), cameraConfig));

        this.cameraConfigDAO.cameraGroups().forEach(cameraGroup -> cameraGroups.put(cameraGroup.getId(), cameraGroup));
    }

    @Override
    public synchronized void addCameraConfig(CameraConfig cameraConfig) {
        stateManager.checkState(State.STARTED);
        logger.debug("Adding camera config - {}. Total: {}", cameraConfig, cameraConfigs.size());
        cameraConfigs.put(cameraConfig.getCameraId(), cameraConfig);
        notifyConfigurationChange();
    }

    @Override
    public synchronized void addPointMapping(int cameraId, PointMapping mappings) {
        stateManager.checkState(State.STARTED);
        cameraConfigs.get(cameraId).setPointMapping(mappings);
        notifyConfigurationChange();
    }

    /**
     * Returns the next camera ID to be taken by a camera
     *
     * @return next camera ID
     */
    @Override
    public synchronized int getNextCameraId() {
        stateManager.checkState(State.STARTED);

        OptionalInt max = cameraConfigs.keySet().stream().mapToInt(Integer::intValue).max();
        int nextInt = 1;
        if (max.isPresent()) {
            nextInt = max.getAsInt() + 1;
        }

        return nextInt;
    }

    @Override
    public CameraConfig getCameraConfig(int cameraId) {
        return cameraConfigs.get(cameraId).clone();
    }

    @Override
    public Map<Integer, CameraConfig> getCameraConfigs() {
        return cameraConfigs;
    }

    @Override
    public BufferedImage getMap() {
        stateManager.checkState(State.STARTED);
        ColorModel cm = map.getColorModel();
        boolean isAlphaPreMultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = map.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPreMultiplied, null);
    }

    @Override
    public List<Zone> getZones() {
        return zones;
    }

    @Override
    public PointMapping getPointMapping(int cameraId) {
        return cameraConfigs.get(cameraId).getPointMapping();
    }

    @Override
    public Set<Integer> getCameraIds() {
        return cameraConfigs.keySet();
    }

    @Override
    public Set<InetSocketAddress> getCameraIpAndPorts() {
        return cameraConfigs.values().stream()
                .map(config -> {
                    String[] parts = config.getIpAndPort().split(":");
                    return InetSocketAddress.createUnresolved(parts[0], Integer.parseInt(parts[1]));
                }).collect(Collectors.toSet());
    }

    @Override
    public int getNumberOfCameras() {
        return cameraConfigs.entrySet().size();
    }

    @Override
    public synchronized void addConfigurationListener(ConfigurationListener listener) {
        configurationListeners.add(listener);
    }

    @Override
    public synchronized void removeConfigurationListener(ConfigurationListener listener) {
        configurationListeners.remove(listener);
    }

    @Override
    public boolean isConfigured() {
        return cameraConfigs.values().size() > 0 && cameraConfigs.values().stream()
                .noneMatch(cameraConfig -> cameraConfig.getCameraGroup() == null);
    }

    private void notifyConfigurationChange() {
        logger.debug("Notifying configuration change");
        configurationListeners.forEach(listener -> listener.configurationChanged(this));
    }

    @Override
    public void stop() {
        stateManager.checkState(State.STARTED);
        logger.info("Stopping Configuration Manager ...");
        stateManager.setState(State.STOPPED);
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public void setConfigurationListeners(Set<ConfigurationListener> configurationListeners) {
        this.configurationListeners = configurationListeners;
    }

    public void setZoneDAO(ZoneDAO zoneDAO) {
        this.zoneDAO = zoneDAO;
    }

    public void setCameraConfigDAO(CameraConfigDAO cameraConfigDAO) {
        this.cameraConfigDAO = cameraConfigDAO;
    }

    public Map<Integer, CameraGroup> getCameraGroups() {
        return cameraGroups;
    }
}
