/*
 * Copyright to Eduze@UoM 2017
 */

package org.eduze.fyp.restapi.resources;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FrameInfo {

    private Camera camera;
    private long timestamp;
    private float[][] coordinates;

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float[][] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(float[][] coordinates) {
        this.coordinates = coordinates;
    }

    public String toString() {
        return String.format("{ camera : %s, timestamp : %d}", camera, timestamp);
    }
}
