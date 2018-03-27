/*
 * Copyright (c) 2018 Augur Analytics
 */

package org.eduze.fyp.core.reid;

import org.eduze.fyp.api.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link org.tensorflow.TensorFlow} based re-identifications platform
 *
 * @author Imesha Sudasingha
 */
public class ReIdentifier {

    private static final Logger logger = LoggerFactory.getLogger(ReIdentifier.class);

    private static final int IMAGE_WIDTH = 60;
    private static final int IMAGE_HEIGHT = 160;

    private List<GalleryPerson> gallery = new ArrayList<>();
    private SavedModelBundle savedModelBundle;
    private Session session;
    private int id = 0;

    public ReIdentifier() {
        this.savedModelBundle = SavedModelBundle.load("re_id/model", "serve");
        this.session = savedModelBundle.session();
    }

    public int identify(BufferedImage image, List<Integer> candidate_indices) {
        image = ImageUtils.resize(image, IMAGE_WIDTH, IMAGE_HEIGHT);
        float[][][] matrix = ImageUtils.imageToMatrix(image);

        List<GalleryComparisonResult> comparisonResults = new ArrayList<>();
        for (GalleryPerson person : gallery) {
            if (!candidate_indices.contains(person.getId()))
                continue;

            float confidence = getConfidence(matrix, person.getMatrixImage());
            GalleryComparisonResult result = new GalleryComparisonResult(person, confidence);
            comparisonResults.add(result);
        }

        Optional<GalleryComparisonResult> possibleMatch = comparisonResults.stream()
                .max(Comparator.comparingDouble(GalleryComparisonResult::getConfidence));

        if (possibleMatch.isPresent() && possibleMatch.get().getConfidence() > 0.9) {
            //            logger.debug("Found match: {}", possibleMatch.get().getGalleryPerson().getId());
            return possibleMatch.get().getGalleryPerson().getId();
        } else {
            this.id++;
            GalleryPerson person = new GalleryPerson(id, image, matrix);
            gallery.add(person);
            return person.getId();
        }
    }

    private float getConfidence(float[][][] image1, float[][][] image2) {
        float[][][][][] input = new float[][][][][]{
                {image1},
                {image2}
        };

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

        return positive / (positive + negative);
    }


    public void close() {
        savedModelBundle.close();
        logger.info("Stopping re-identifier");
    }
}
