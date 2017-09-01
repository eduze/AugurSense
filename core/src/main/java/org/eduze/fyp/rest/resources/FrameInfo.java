/*
 * Copyright to Eduze@UoM 2017
 */

package org.eduze.fyp.rest.resources;

import org.eduze.fyp.api.resources.PersonCoordinate;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class FrameInfo {

    private Camera camera;
    private long timestamp;
    private List<PersonCoordinate> personCoordinates;

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

    public List<PersonCoordinate> getPersonCoordinates() {
        return personCoordinates;
    }

    public void setPersonCoordinates(List<PersonCoordinate> personCoordinates) {
        this.personCoordinates = personCoordinates;
    }

    public String toString() {
        return String.format("{ camera : %s, timestamp : %d}", camera, timestamp);
    }
}
