package org.eduze.fyp.core.util;

import org.eduze.fyp.core.resources.PersonLocation;
import org.eduze.fyp.api.resources.Coordinate;
import org.eduze.fyp.api.resources.PersonCoordinate;

import java.util.ArrayList;
import java.util.List;

public class AccuracyTester {
    StringBuilder reportedPairsText = new StringBuilder();
    List<List<Coordinate>> reportedPairs = new ArrayList<>();
    private boolean testMapMergeAccuracy = false;

    public boolean isTestMapMergeAccuracy() {
        return testMapMergeAccuracy;
    }

    public void setTestMapMergeAccuracy(boolean testMapMergeAccuracy) {
        this.testMapMergeAccuracy = testMapMergeAccuracy;
    }

    public void reportPointDeviation(PersonCoordinate p1, PersonLocation p2, int cameraId, long timestamp){
        //find a matching past point
        if (!testMapMergeAccuracy)
            return;

        p2.getContributingCoordinates().forEach((k,v)->{
            if(k != cameraId){
                if(p1.getTimestamp() == v.getTimestamp())
                {
                    List<Coordinate> reportedPair = new ArrayList<>();

                    reportedPair.add(p1);
                    reportedPair.add(v);
                    this.reportedPairs.add(reportedPair);

                    reportedPairsText.append(p1.getX());
                    reportedPairsText.append("\t");
                    reportedPairsText.append(p1.getY());
                    reportedPairsText.append("\t");
                    reportedPairsText.append(v.getX());
                    reportedPairsText.append("\t");
                    reportedPairsText.append(v.getY());
                    reportedPairsText.append("\t");
                    double distanceX = p1.getX() - v.getX();
                    double distanceY = p1.getY() - v.getY();
                    double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                    reportedPairsText.append(distance);
                    reportedPairsText.append("\n");

                    System.out.println(reportedPairsText.toString());;
                }
            }
        });



    }
}
