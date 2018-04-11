/*
 * Copyright (c) 2018 Augur Analytics
 */

/*
 * <Paste your header here>
 */
package org.augur.sense.core.reid;

import java.awt.image.BufferedImage;

public class GalleryPerson {

    private int id;
    private BufferedImage image;
    private float[][][] matrixImage;

    public GalleryPerson(int id, BufferedImage image, float[][][] matrixImage) {
        this.id = id;
        this.image = image;
        this.matrixImage = matrixImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BufferedImage getImage() {
        return image;
    }

    public float[][][] getMatrixImage() {
        return matrixImage;
    }
}
