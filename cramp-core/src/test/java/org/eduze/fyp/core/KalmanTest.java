/*
 * Copyright (c) 2018 Augur Analytics
 */

/*
 * <Paste your header here>
 */
package org.eduze.fyp.core;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;

public class KalmanTest {

    public static void main(String[] args) {
        // A = [ 1 ]
        RealMatrix A = new Array2DRowRealMatrix(new double[][]{
                {1d, 0d},
                {0d, 1d}
        });
        // no control input
        RealMatrix B = null;
        // H = [ 1 ]
        RealMatrix H = new Array2DRowRealMatrix(new double[][]{
                {1d},
                {1d}
        });
        // Q = [ 0 ]
        RealMatrix Q = new Array2DRowRealMatrix(new double[][]{
                {0d},
                {0d}
        });
        // R = [ 0 ]
        RealMatrix R = new Array2DRowRealMatrix(new double[][]{
                {0},
                {0}
        });

        ProcessModel processModel = new DefaultProcessModel(A, B, Q, new ArrayRealVector(new double[]{0, 0}), null);
        MeasurementModel measurementModel = new DefaultMeasurementModel(H, R);
        KalmanFilter kalmanFilter = new KalmanFilter(processModel, measurementModel);

        double[][] locations = new double[][]{{1d, 2d}, {2d, 3d}, {3d, 4d}, {4d, 5d}};

        for (double[] location : locations) {
            kalmanFilter.predict();
            double[] stateEstimation = kalmanFilter.getStateEstimation();

            kalmanFilter.correct(new ArrayRealVector(new double[]{location[0], location[1]}));
        }
    }
}
