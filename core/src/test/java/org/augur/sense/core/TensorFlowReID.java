/*
 * Copyright (c) 2018 Augur Analytics
 */

package org.augur.sense.core;

import org.augur.sense.api.util.ImageUtils;
import org.augur.sense.api.util.ImageUtils;
import org.junit.Test;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.Tensors;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TensorFlowReID {

    @Test
    public void helloWorldTest() throws Exception {
        try (Graph g = new Graph()) {
            final String value = "Hello from " + TensorFlow.version();

            // Construct the computation graph with a single operation, a constant
            // named "MyConst" with a value "value".
            try (Tensor t = Tensor.create(value.getBytes("UTF-8"))) {
                // The Java API doesn't yet include convenience functions for adding operations.
                g.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
            }

            // Execute the "MyConst" operation in a Session.
            try (Session s = new Session(g);
                 Tensor output = s.runner().fetch("MyConst").run().get(0)) {
                System.out.println(new String(output.bytesValue(), "UTF-8"));
            }
        }
    }

    @Test
    public void reIdTest() throws Exception {
        BufferedImage img1 = ImageIO.read(new FileInputStream("cramp-ui/src/main/resources/CRAMP_re_id/gallery/12783_312.jpg"));
        BufferedImage img2 = ImageIO.read(new FileInputStream("cramp-ui/src/main/resources/CRAMP_re_id/gallery/797898_71.jpg"));
        BufferedImage img3 = ImageIO.read(new FileInputStream("cramp-ui/src/main/resources/CRAMP_re_id/gallery/119367_470.jpg"));
        BufferedImage img4 = ImageIO.read(new FileInputStream("cramp-ui/src/main/resources/CRAMP_re_id/gallery/55422_370.jpg"));
        BufferedImage img5 = ImageIO.read(new FileInputStream("cramp-ui/src/main/resources/CRAMP_re_id/gallery/998966_290.jpg"));

        BufferedImage[] gallery = new BufferedImage[]{img1, img2, img3, img4, img5};
        try (SavedModelBundle bundle = SavedModelBundle.load("sense/re_id/cvpr_reid/model/1", "serve")) {
            Session session = bundle.session();
            Graph graph = bundle.graph();

            long start = new Date().getTime();
            runReId(graph, session, img1, img2);
            runReId(graph, session, img1, img3);
            runReId(graph, session, img1, img4);
            runReId(graph, session, img1, img5);
            long end = new Date().getTime();
            System.out.printf("Average time per comparison: %d ms\n", ((end - start) / 4));

            for (BufferedImage target : gallery) {
                start = new Date().getTime();
                reIdGallery(graph, session, gallery, target);
                end = new Date().getTime();
                System.out.printf("Time for comparison: %d ms\n", (end - start));
            }
        }
    }


    private static void reIdGallery(Graph graph, Session session, BufferedImage[] gallery, BufferedImage target) throws Exception {
        float[] confidences = new float[gallery.length];
        for (int i = 0; i < gallery.length; i++) {
            confidences[i] = runReId(graph, session, gallery[i], target);
        }
        System.out.println(Arrays.toString(confidences));
    }

    private static float runReId(Graph graph, Session session, BufferedImage img1, BufferedImage img2) {
        int IMAGE_WIDTH = 60;
        int IMAGE_HEIGHT = 160;

        img1 = ImageUtils.resize(img1, IMAGE_WIDTH, IMAGE_HEIGHT);
        img2 = ImageUtils.resize(img2, IMAGE_WIDTH, IMAGE_HEIGHT);
        float[][][] image1 = imageToMatrix(img1);
        float[][][] image2 = imageToMatrix(img2);
        float[][][][][] input = new float[][][][][]{
                {image1},
                {image2}
        };

        Operation inference = graph.operation("Inference_Final");

        Tensor<Float> testImages = Tensors.create(input);
        Tensor<Boolean> isTrain = Tensors.create(false);
        List<Tensor<?>> predictions = session.runner().feed("images", testImages)
                .feed("is_train", isTrain)
                .fetch("Inference_Final")
                .run();

        float[][] prediction = new float[1][2];
        predictions.get(0).copyTo(prediction);
        float positive = prediction[0][0];
        float negative = prediction[0][1];
        float confidence = positive / (positive + negative);

        System.out.printf("Confidence is: %f\n", confidence);
        return confidence;
    }

    private static float[][][] imageToMatrix(BufferedImage bi) {
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