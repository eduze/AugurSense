/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.core;

import javax.swing.*;
import java.awt.*;

public class AnalyticsEngineRunner {

    public static void main(String[] args) {
        final AnalyticsEngine engine = new AnalyticsEngine();
        engine.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                engine.stop();
            }
        });

        while (true) {
            try {
                Thread.currentThread().join();
                break;
            } catch (InterruptedException ignored) {
            }
        }

//        JFrame frame = new JFrame();
//
//        JLabel imageLabel = new JLabel(new ImageIcon(engine.getConfigManager().getMap()));
//        frame.getContentPane().add(imageLabel, BorderLayout.CENTER);
//        frame.setSize(300, 400);
//        frame.setVisible(true);
//
//        JPanel mainPanel = new JPanel(new BorderLayout());
//        mainPanel.add(imageLabel);
//        frame.add(mainPanel);
//        frame.setVisible(true);
    }

}
