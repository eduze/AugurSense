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

package org.eduze.fyp.api.resources;

import org.eduze.fyp.api.model.Person;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PersonCoordinate extends Coordinate {

    private double standProbability = 0;
    private double sitProbability = 0;
    private double headDirectionY = 0;
    private double headDirectionX = 0;
    private byte[] image;

    public PersonCoordinate() { }

    public PersonCoordinate(Person p) {
        this(p.getX(), p.getY(), p.getTimestamp().getTime(), p.getStandProbability(), p.getSitProbability(),
                p.getHeadDirectionX(), p.getHeadDirectionY(), p.getImage());
    }

    public PersonCoordinate(double x, double y, long timestamp, double standProbability, double sitProbability,
            double headDirectionY, double headDirectionX, byte[] image) {
        super(x, y, timestamp);
        this.standProbability = standProbability;
        this.sitProbability = sitProbability;
        this.headDirectionY = headDirectionY;
        this.headDirectionX = headDirectionX;
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public double getStandProbability() {
        return standProbability;
    }

    public void setStandProbability(double standProbability) {
        this.standProbability = standProbability;
    }

    public double getSitProbability() {
        return sitProbability;
    }

    public void setSitProbability(double sitProbability) {
        this.sitProbability = sitProbability;
    }

    public double getHeadDirectionY() {
        return headDirectionY;
    }

    public void setHeadDirectionY(double headDirectionY) {
        this.headDirectionY = headDirectionY;
    }

    public double getHeadDirectionX() {
        return headDirectionX;
    }

    public void setHeadDirectionX(double headDirectionX) {
        this.headDirectionX = headDirectionX;
    }

    public Coordinate toCoordinate() {
        return new Coordinate(getX(), getY(), getTimestamp());
    }
}
