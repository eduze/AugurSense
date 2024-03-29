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
package org.augur.sense.api.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A utility class to handle {@link BufferedImage}s and other types of images related instances
 */
public class ImageUtils {

    private ImageUtils() {
    }

    public static byte[] bufferedImageToByteArray(BufferedImage image) throws IOException {
        return bufferedImageToByteArray(image, "jpg");
    }

    public static byte[] bufferedImageToByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        outputStream.flush();
        byte[] imageBytes = outputStream.toByteArray();
        outputStream.close();
        return imageBytes;
    }

    public static BufferedImage byteArrayToBufferedImage(byte[] array) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(array)) {
            return ImageIO.read(inputStream);
        }
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static byte[] resize(byte[] byteArray, int newW, int newH) throws IOException {
        BufferedImage image = byteArrayToBufferedImage(byteArray);
        BufferedImage resized = resize(image, newW, newH);
        return bufferedImageToByteArray(resized);
    }

    public static float[][][] imageToMatrix(BufferedImage bi) {
        float[][][] C = new float[bi.getHeight()][bi.getWidth()][3];
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                int rgb = bi.getRGB(j, i);

                C[i][j][0] = (rgb >> 16) & 0x000000FF;
                C[i][j][1] = (rgb >> 8) & 0x000000FF;
                C[i][j][2] = (rgb) & 0x000000FF;
            }
        }

        return C;
    }
}
