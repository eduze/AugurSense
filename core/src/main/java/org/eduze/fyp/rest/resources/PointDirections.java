package org.eduze.fyp.rest.resources;

import java.util.ArrayList;
import java.util.List;

public class PointDirections {
    private double x;
    private double y;

    private List<Double> directionCountList;
    private List<Double> directionVelocityList;

    private List<Double> normalizedDirectionCountList;
    private List<Double> normalizedDirectionVelocityList;

    private List<Double> headDirectionList;

    private int directionCount = 16;

    private int headDirectionContributorCount = 0;

    public int getDirectionCount() {
        return directionCount;
    }

    public List<Double> getHeadDirectionList() {
        return headDirectionList;
    }

    public void setHeadDirectionList(List<Double> headDirectionList) {
        this.headDirectionList = headDirectionList;
    }

    public PointDirections(double x, double y, int directionCount){
        this.x = x;
        this.y = y;
        this.directionCount = directionCount;
        this.directionCountList = new ArrayList<>();
        this.directionVelocityList = new ArrayList<>();
        this.headDirectionList = new ArrayList<>();

        for(int i = 0;i < directionCount; i++){
            this.directionCountList.add(0.0);
            this.directionVelocityList.add(0.0);
            this.headDirectionList.add(0.0);
        }
    }

    private int decodeIndex(double angle)
    {
        double slotIndexD = (angle % 360) * directionCount / 360;
        long slotIndex = Math.round(slotIndexD);
        slotIndex = slotIndex % directionCount;
        return (int) slotIndex;
    }

    public double incrementDirectionCount(double angle, double value){
        int index = decodeIndex(angle);
        this.directionCountList.set(index,this.directionCountList.get(index) + value);
        return this.directionCountList.get(index);
    }

    public double incrementHeadDirection(double angle, double value){
        int index = decodeIndex(angle);
        this.headDirectionList.set(index,this.headDirectionList.get(index) + value);
        this.headDirectionContributorCount++;
        return this.headDirectionList.get(index);
    }

    public double incrementDirectionVelocity(double angle, double value){
        int index = decodeIndex(angle);
        this.directionVelocityList.set(index,this.directionVelocityList.get(index) + value);
        return this.directionVelocityList.get(index);
    }

    public void setHeadDirection(double angle, double value){
        int index = decodeIndex(angle);
        this.headDirectionList.set(index,value);
    }

    public void setDirectionCount(double angle, double value){
        int index = decodeIndex(angle);
        this.directionCountList.set(index,value);
    }

    public void setDirectionVelocity(double angle, double value){
        int index = decodeIndex(angle);
        this.directionVelocityList.set(index,value);
    }

    public double readDirectionCount(double angle){
       return directionCountList.get(decodeIndex(angle));
    }

    public double readDirectionVelocity(double angle){
        return directionVelocityList.get(decodeIndex(angle));
    }

    public double readHeadDirection(double angle){
        return headDirectionList.get(decodeIndex(angle));
    }

    public List<Double> getDirectionCountList() {
        return directionCountList;
    }

    public void setDirectionCountList(List<Double> directionCountList) {
        this.directionCountList = directionCountList;
    }

    public List<Double> getDirectionVelocityList() {
        return directionVelocityList;
    }

    public void setDirectionVelocityList(List<Double> directionVelocityList) {
        this.directionVelocityList = directionVelocityList;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public List<Double> getNormalizedDirectionVelocityList() {
        return normalizedDirectionVelocityList;
    }

    public void setNormalizedDirectionVelocityList(List<Double> normalizedDirectionVelocityList) {
        this.normalizedDirectionVelocityList = normalizedDirectionVelocityList;
    }

    public List<Double> getNormalizedDirectionCountList() {
        return normalizedDirectionCountList;
    }

    public void setNormalizedDirectionCountList(List<Double> normalizedDirectionCountList) {
        this.normalizedDirectionCountList = normalizedDirectionCountList;
    }

    public void normalize(double maxCount, double maxVelocity) {
        this.normalizedDirectionCountList = new ArrayList<>();
        this.directionCountList.forEach((v)->{
            this.normalizedDirectionCountList.add(v/maxCount);
        });

        for(int i = 0; i < this.directionCountList.size(); i++)
        {
            if(this.directionCountList.get(i) > 0)
                this.directionVelocityList.set(i,this.directionVelocityList.get(i)/this.directionCountList.get(i)); // Calculate average velocity
        }

        for(int i = 0; i < this.headDirectionList.size(); i++)
        {
            if(this.headDirectionContributorCount > 0)
                this.headDirectionList.set(i,this.headDirectionList.get(i)/this.headDirectionContributorCount); // Normalize head direction by diving number of people
        }

        this.normalizedDirectionVelocityList = new ArrayList<>();
        this.directionVelocityList.forEach((v)->{
            this.normalizedDirectionVelocityList.add(v/maxVelocity);
        });
    }
}
