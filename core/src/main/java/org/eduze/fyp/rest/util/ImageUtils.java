/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.rest.util;

import javax.imageio.ImageIO;
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
}
