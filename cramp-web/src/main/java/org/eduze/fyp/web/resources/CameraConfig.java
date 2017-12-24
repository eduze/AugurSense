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
package org.eduze.fyp.web.resources;

import org.eduze.fyp.api.resources.PointMapping;

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

    private PointMapping initialMapping = null;

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

    public PointMapping getInitialMapping() {
        return initialMapping;
    }

    public void setInitialMapping(PointMapping initialMapping) {
        this.initialMapping = initialMapping;
    }
}
