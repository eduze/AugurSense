/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.ui;

import org.eduze.fyp.core.AnalyticsEngine;
import org.eduze.fyp.core.config.ConfigManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AppTest {

    private static final String[] views = new String[]{"views/view1.png", "views/view2.jpg"};

    public static void main(String[] args) throws IOException {
        ConfigManager configManager = AnalyticsEngine.getInstance().getConfigManager();

        for (int i = 0; i < views.length; i++) {
            try (InputStream inputStream = new FileInputStream(views[i])) {
                BufferedImage cameraView = ImageIO.read(inputStream);
                configManager.setCameraView(i, cameraView);
            }
        }

        App.main(new String[0]);
    }
}
