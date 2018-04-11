/*
 * Copyright 2017 Eduze
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

package org.augur.sense.web.resources;

import org.augur.sense.api.resources.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DirectionMap {
    private HashMap<String,PointDirections> pointDirections;
    private int cellSize = 10;

    private static final Logger logger = LoggerFactory.getLogger(DirectionMap.class);

    private int directionCount = 16;

    public DirectionMap(){
        this.pointDirections = new HashMap<>();
    }

    public List<PointDirections> getPointDirections(){
        ArrayList<PointDirections> results = new ArrayList<>();
        results.addAll(this.pointDirections.values());
        return results;
    }

    public DirectionMap(int cellSize, int directionCount){
        this.pointDirections = new HashMap<>();
        this.cellSize = cellSize;
        this.directionCount = directionCount;
    }

    public PointDirections getCell(int x, int y){
        int offsetX = x%cellSize;
        int startX = x - offsetX;

        int offsetY = y%cellSize;
        int startY = y - offsetY;

        PointDirections result = pointDirections.getOrDefault(startX +"_" +startY,new PointDirections(startX,startY,directionCount));
        pointDirections.put(startX +"_" +startY,result);
        return result;
    }

    public void normalize(){
        final double[] maxCount = {0};
        final double[] maxVelocity = {0};
        pointDirections.forEach((k,v)->{
            v.getDirectionCountList().forEach((d)->{
                if(d > maxCount[0]){
                    maxCount[0] = d;
                }
            });
            v.getDirectionVelocityList().forEach((d)->{
                if(d> maxVelocity[0])
                    maxVelocity[0] = d;
            });
        });

        if(maxCount[0] == 0){
            maxCount[0] = 1; //Just copy the zeroes forward
        }
        if(maxVelocity[0] == 0){
            maxVelocity[0] = 1;
        }

        pointDirections.forEach((k,v)->{
            v.normalize(maxCount[0],maxVelocity[0]);
        });
    }

    public static double computeAngle(double x, double y){
        double answer = Math.toDegrees(Math.atan2(y, x)); //To be compatible with ui, y is negated
        answer = answer % 360;
        if(answer < 0)
            answer += 360;
        answer = answer % 360;

        //logger.info(x + " " + y + " " + answer);
        return answer;
    }

    public void appendEdge(Point p1, Point p2, double velocity, double count, Point headDirection){
        double cx = p2.getX() - p1.getX();
        double cy = p2.getY() - p1.getY();
        double length = Math.sqrt(cx*cx+cy*cy);
        double segmentCount = length / cellSize * Math.sqrt(2);

        double dx = cx/length;
        double dy = cy/length;

        Set<PointDirections> processedCoordinates = new HashSet<>();
        for(int i = 0; i <= segmentCount; i++){
            int targetX = (int)Math.round(p1.getX() + (i/segmentCount * length * dx));
            int targetY = (int)Math.round(p1.getY() + (i/segmentCount * length * dy));
            PointDirections target = getCell(targetX,targetY);
            if(!processedCoordinates.contains(target)){
                processedCoordinates.add(target);
                target.incrementDirectionCount(computeAngle(dx,dy),count);
                target.incrementDirectionVelocity(computeAngle(dx,dy),velocity);
                if(headDirection != null && (headDirection.getX() > 0 || headDirection.getY() > 0))
                {
                    target.incrementHeadDirection(computeAngle(headDirection.getX(),headDirection.getY()),1);
                }

            }
        }
    }

    public static void main(String[] args) {
        DirectionMap.computeAngle(0,1);
        DirectionMap.computeAngle(0,-1);

        DirectionMap dm = new DirectionMap(10,16);
        dm.appendEdge(new Point(20,-20),new Point(50,50),1,1,new Point(0,0));

    }
}
