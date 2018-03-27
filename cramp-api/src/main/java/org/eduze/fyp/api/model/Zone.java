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

package org.eduze.fyp.api.model;

import org.eduze.fyp.api.model.helpers.ZoneCoordinatesConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Entity
@Table(name = "zones")
@XmlRootElement
public class Zone {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String zoneName;
    private int zoneLimit;

    @Convert(converter = ZoneCoordinatesConverter.class)
    private List<Integer> xCoordinates;

    @Convert(converter = ZoneCoordinatesConverter.class)
    private List<Integer> yCoordinates;

    @ManyToOne(optional = false)
    private CameraGroup cameraGroup;

    public int getId() {
        return id;
    }

    public String getZoneName() {
        return zoneName;
    }

    @XmlElement(name = "xCoordinates")
    public List<Integer> getXCoordinates() {
        return xCoordinates;
    }

    @XmlElement(name = "yCoordinates")
    public List<Integer> getYCoordinates() {
        return yCoordinates;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public void setXCoordinates(List<Integer> xCoordinates) {
        this.xCoordinates = xCoordinates;
    }

    public void setYCoordinates(List<Integer> yCoordinates) {
        this.yCoordinates = yCoordinates;
    }

    public int getZoneLimit() {
        return zoneLimit;
    }

    public void setZoneLimit(int zoneLimit) {
        this.zoneLimit = zoneLimit;
    }

    public CameraGroup getCameraGroup() {
        return cameraGroup;
    }

    public void setCameraGroup(CameraGroup cameraGroup) {
        this.cameraGroup = cameraGroup;
    }

    @Override
    public String toString() {
        return String.format("%s -> [%s and %s]", zoneName, xCoordinates, yCoordinates);
    }
}
