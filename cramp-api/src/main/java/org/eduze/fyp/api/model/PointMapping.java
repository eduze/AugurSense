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
package org.eduze.fyp.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eduze.fyp.api.model.helpers.PointListToStringConverter;
import org.eduze.fyp.api.resources.Point;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to represent camera space to world space mappings of reference points (4 reference points usually). Points are
 * kept as x and y coordinates.
 *
 * @author Imesha Sudasingha
 */
@Entity
@Table(name = "point_mappings")
@JsonIgnoreProperties("cameraConfig")
public class PointMapping implements Cloneable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Convert(converter = PointListToStringConverter.class)
    private List<Point> screenSpacePoints = new ArrayList<>(4);

    @Convert(converter = PointListToStringConverter.class)
    private List<Point> worldSpacePoints = new ArrayList<>(4);

    @XmlTransient
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "camera_config_id", nullable = false)
    private CameraConfig cameraConfig;

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

    public void setScreenSpacePoints(List<Point> screenSpacePoints) {
        this.screenSpacePoints = screenSpacePoints;
    }

    public void setWorldSpacePoints(List<Point> worldSpacePoints) {
        this.worldSpacePoints = worldSpacePoints;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public void setCameraConfig(CameraConfig cameraConfig) {
        this.cameraConfig = cameraConfig;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public PointMapping clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException ignored) { }

        PointMapping pointMapping = new PointMapping();
        pointMapping.setCameraConfig(getCameraConfig());
        // Not sending ID since clone used for write tasks
        //        pointMapping.setId(getId());
        pointMapping.setScreenSpacePoints(getScreenSpacePoints().stream().map(Point::clone).collect(Collectors.toList()));
        pointMapping.setWorldSpacePoints(getWorldSpacePoints().stream().map(Point::clone).collect(Collectors.toList()));
        return pointMapping;
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
