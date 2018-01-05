/*
 * Copyright 2018 Eduze
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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

/**
 * Configuration on a given camera's view.
 * <pre>
 *     {
 *         cameraId: 1
 *         ipAndPort: 192.168.1.1:8001,
 *         view: [...]
 *     }
 * </pre>
 *
 * @author Imesha Sudasingha
 */
@XmlRootElement
public class CameraConfig {
    private int cameraId;
    private String ipAndPort;
    private byte[] view;
    private PointMapping pointMapping = new PointMapping();

    public byte[] getView() {
        return view;
    }

    public void setView(byte[] bytes) throws IOException {
        this.view = bytes;
    }

    public String getIpAndPort() {
        return ipAndPort;
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

    public PointMapping getPointMapping() {
        return pointMapping;
    }

    public void setPointMapping(PointMapping pointMapping) {
        this.pointMapping = pointMapping;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public String toString() {
        return String.format("{ camera : %s, ipAndPort : %s, pointMappings: %s }", cameraId, ipAndPort, pointMapping);
    }
}
