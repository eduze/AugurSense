/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core.util;

import java.util.ArrayList;
import java.util.List;

public class PointMapping {

    private List<Point> screenSpacePoints = new ArrayList<>(4);
    private List<Point> worldSpacePoints = new ArrayList<>(4);

    public void addScreenSpacePoint(Point point) {
        if (screenSpacePoints.size() == 4) {
            throw new IllegalArgumentException("This mapping already have 4 screeen space points");

        }
        if (!screenSpacePoints.contains(point)) {
            screenSpacePoints.add(point);
        }
    }

    public void addWorldSpacePoint(Point point) {
        if (worldSpacePoints.size() == 4) {
            throw new IllegalArgumentException("This mapping already have 4 screen world points");
        }

        if (!worldSpacePoints.contains(point)) {
            worldSpacePoints.add(point);
        }
    }

    public List<Point> getScreenSpacePoints() {
        return screenSpacePoints;
    }

    public List<Point> getWorldSpacePoints() {
        return worldSpacePoints;
    }
}
