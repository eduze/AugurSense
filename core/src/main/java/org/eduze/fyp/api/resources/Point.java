/*
 * Copyright to Eduze@UoM 2017
 */

package org.eduze.fyp.api.resources;

/**
 * Represents an <pre>(x,y)</pre> coordinate pair.
 *
 * @author Imesha Sudasingha
 */
public class Point {
    private double x;
    private double y;

    public Point() {
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("[%f,%f]", x, y);
    }
}
