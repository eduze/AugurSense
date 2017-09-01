/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.rest.resources;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

@XmlRootElement
public class CameraView {

    private Camera camera;
    private byte[] viewBytes;

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public byte[] getViewBytes() {
        return viewBytes;
    }

    public void setViewBytes(byte[] bytes) throws IOException {
        this.viewBytes = bytes;
    }

    public String toString() {
        return String.format("{ camera : %s, bytesLength : %d }", camera, viewBytes.length);
    }
}
