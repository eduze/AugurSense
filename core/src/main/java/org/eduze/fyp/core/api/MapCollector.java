/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core.api;

import org.eduze.fyp.core.api.config.Startable;
import org.eduze.fyp.core.api.listeners.ConfigurationListener;
import org.eduze.fyp.core.api.listeners.MapListener;
import org.eduze.fyp.core.api.resources.LocalMap;

import java.util.Set;

public interface MapCollector extends ConfigurationListener, Startable {

    void addMapListener(MapListener listener);

    void removeMapListener(MapListener listener);

    void addPoints(LocalMap map);

    void publishMaps(Set<LocalMap> maps);
}
