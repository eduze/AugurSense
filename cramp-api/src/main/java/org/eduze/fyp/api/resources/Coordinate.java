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

package org.eduze.fyp.api.resources;

public class Coordinate extends Point {

    private long timestamp;

    public Coordinate() {
        super(0, 0);
    }

    public Coordinate(double x, double y, long timestamp, double sitProbability, double standProbability, double headDirectionX, double headDirectionY) {
        super(x, y);
        this.timestamp = timestamp;
        this.headDirectionX =headDirectionX;
        this.headDirectionY = headDirectionY;
        this.sitProbability = sitProbability;
        this.standProbability = standProbability;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private double standProbability = 0;

    private double sitProbability = 0;

    private double headDirectionY = 0;
    private double headDirectionX = 0;

    public double getHeadDirectionX() {
        return headDirectionX;
    }

    public double getHeadDirectionY() {
        return headDirectionY;
    }


    public double getSitProbability() {
        return sitProbability;
    }

    public double getStandProbability() {
        return standProbability;
    }

    public void setHeadDirectionX(double headDirectionX) {
        this.headDirectionX = headDirectionX;
    }

    public void setHeadDirectionY(double headDirectionY) {
        this.headDirectionY = headDirectionY;
    }

    public void setSitProbability(double sitProbability) {
        this.sitProbability = sitProbability;
    }

    public void setStandProbability(double standProbability) {
        this.standProbability = standProbability;
    }
}
