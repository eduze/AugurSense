/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core.api;

import org.eduze.fyp.core.api.listeners.ConfigurationListener;
import org.eduze.fyp.core.api.listeners.DataListener;

import java.util.List;

public interface DataCollector extends ConfigurationListener {

    void addDataListener(DataListener listener);

    void removeDataListener(DataListener listener);

    void addPoints(long timestamp, List<Point> coordinates);
}
