/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package org.eduze.fyp.rest.services;

import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.listeners.ProcessedMapListener;
import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.impl.db.dao.PersonDAO;
import org.eduze.fyp.impl.db.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalyticsService implements ProcessedMapListener {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private List<List<PersonSnapshot>> snapshots = new ArrayList<>();
    private PersonDAO personDAO;
    private ConfigurationManager configurationManager;
    private int mapWidth = -1;
    private int mapHeight = -1;

    public AnalyticsService() {
    }

    public List<List<PersonSnapshot>> getRealTimeMap() {
        return snapshots;
    }

    public int[][] getHeatMap(long fromTimestamp, long toTimestamp) {
        if (mapHeight == -1 || mapWidth == -1) {
            mapWidth = configurationManager.getMap().getWidth();
            mapHeight = configurationManager.getMap().getHeight();
        }

        logger.debug("Generating heat map for {}-{} (Map - {}x{})", fromTimestamp, toTimestamp, mapWidth, mapHeight);
        int[][] heatmap = new int[mapHeight][mapWidth];

        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);
        List<Person> people = personDAO.list(from, to);
        if (people == null || people.size() == 0) {
            return heatmap;
        }

        int resolution = 5;
        AtomicInteger max = new AtomicInteger(0);
        people.forEach(person -> {
            int x = (int) (person.getX() / resolution);
            int y = (int) (person.getY() / resolution);
            heatmap[x][y] += 1;
            max.getAndUpdate(val -> val > heatmap[x][y] ? val : heatmap[x][y]);
        });

        for (int x = 0; x < mapHeight; x++) {
            for (int y = 0; y < mapWidth; y++) {
                heatmap[x][y] = (heatmap[x][y] * 100) / max.get();
            }
        }

        return heatmap;
    }

    @Override
    public void mapProcessed(List<List<PersonSnapshot>> snapshots) {
        this.snapshots = snapshots;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }
}
