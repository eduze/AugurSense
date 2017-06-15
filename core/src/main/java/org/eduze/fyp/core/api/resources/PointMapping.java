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
package org.eduze.fyp.core.api.resources;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Screen : ");
        screenSpacePoints.forEach(point -> sb.append(point.toString()));
        sb.append(" ");
        sb.append("World : ");
        worldSpacePoints.forEach(point -> sb.append(point.toString()));
        return sb.toString();
    }
}
