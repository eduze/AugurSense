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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Entity
@Table(name = "zones")
@XmlRootElement
public class Zone {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String zoneName;
    private String xCoordinates;
    private String yCoordinates;

    public int getId() {
        return id;
    }

    public String getZoneName() {
        return zoneName;
    }

    @XmlElement(name = "xCoordinates")
    public int[] getXCoordinates() {
        return getCoordinates(xCoordinates);
    }

    @XmlElement(name = "yCoordinates")
    public int[] getYCoordinates() {
        return getCoordinates(yCoordinates);
    }

    private int[] getCoordinates(String coordinateField) {
        if (Objects.equals(coordinateField, ""))
            return new int[0];
        String[] strCoordinates = coordinateField.split(",");
        int[] coordinates = new int[strCoordinates.length];
        for (int i = 0; i < coordinates.length; i++)
            coordinates[i] = Integer.valueOf(strCoordinates[i].trim());
        return coordinates;
    }

    @Override
    public String toString() {
        return String.format("%s -> [%s and %s]", zoneName, xCoordinates, yCoordinates);
    }
}
