/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.rest.resources;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

/**
 * Configuration on a given camera's view.
 * <pre>
 *     {
 *         camera: {
 *             id: 1
 *         },
 *         ipAndPort: 192.168.1.1:8001,
 *         viewBytes: [...]
 *     }
 * </pre>
 *
 * @author Imesha Sudasingha
 */
@XmlRootElement
public class CameraConfig {
    private Camera camera;
    private String ipAndPort;
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

    public String getIpAndPort() {
        return ipAndPort;
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

    public String toString() {
        return String.format("{ camera : %s, bytesLength : %d }", camera, viewBytes.length);
    }
}
