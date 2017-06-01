/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi.resources;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@XmlRootElement
public class CameraView {

    private Camera camera;
    private byte[] view;
    private BufferedImage image;

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public byte[] getView() {
        return view;
    }

    public void setView(byte[] bytes) throws IOException {
//        byte[] bytes = DatatypeConverter.parseBase64Binary(base64Image);
        this.image = ImageIO.read(new ByteArrayInputStream(bytes));
        this.view = bytes;
    }
}
