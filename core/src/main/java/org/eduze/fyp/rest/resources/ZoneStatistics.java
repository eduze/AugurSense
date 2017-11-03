package org.eduze.fyp.rest.resources;

import jdk.nashorn.internal.runtime.regexp.joni.constants.StringType;

import java.util.HashMap;
import java.util.Map;

public class ZoneStatistics {
    private double averagePersonCount;
    private double averageSittingCount;
    private double averageStandingCount;
    private double averageUnclassifiedPoseCount;


    private long fromTimestamp;
    private long toTimestamp;

    private Map<Integer,Long> outgoingMap = null;
    private Map<Integer,Long> incomingMap = null;

    private int zoneId;
    private String zoneName;

    private long totalOutgoing = 0;
    private long totalIncoming = 0;


    public double getAverageSittingCount() {
        return averageSittingCount;
    }

    public double getAverageStandingCount() {
        return averageStandingCount;
    }

    public double getAverageUnclassifiedPoseCount() {
        return averageUnclassifiedPoseCount;
    }

    public void setAverageSittingCount(double averageSittingCount) {
        this.averageSittingCount = averageSittingCount;
    }

    public void setAverageStandingCount(double averageStandingCount) {
        this.averageStandingCount = averageStandingCount;
    }

    public void setAverageUnclassifiedPoseCount(double averageUnclassifiedPoseCount) {
        this.averageUnclassifiedPoseCount = averageUnclassifiedPoseCount;
    }

    public long getTotalIncoming() {
        return totalIncoming;
    }

    public long getTotalOutgoing() {
        return totalOutgoing;
    }

    public void setTotalIncoming(long totalIncoming) {
        this.totalIncoming = totalIncoming;
    }

    public void setTotalOutgoing(long totalOutgoing) {
        this.totalOutgoing = totalOutgoing;
    }

    public Map<Integer, Long> getOutgoingMap() {
        return outgoingMap;
    }

    public void setOutgoingMap(Map<Integer, Long> outgoingMap) {
        this.outgoingMap = outgoingMap;
    }

    public Map<Integer, Long> getIncomingMap() {
        return incomingMap;
    }

    public void setIncomingMap(Map<Integer, Long> incomingMap) {
        this.incomingMap = incomingMap;
    }

    public String getZoneName() {
        return zoneName;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public double getAveragePersonCount() {
        return averagePersonCount;
    }

    public long getFromTimestamp() {
        return fromTimestamp;
    }

    public long getToTimestamp() {
        return toTimestamp;
    }

    public void setAveragePersonCount(double averagePersonCount) {
        this.averagePersonCount = averagePersonCount;
    }

    public void setFromTimestamp(long fromTimestamp) {
        this.fromTimestamp = fromTimestamp;
    }

    public void setToTimestamp(long toTimestamp) {
        this.toTimestamp = toTimestamp;
    }

    public ZoneStatistics(int zoneId, String zoneName, long fromTimestamp, long toTimestamp){
        this.fromTimestamp = fromTimestamp;
        this.toTimestamp = toTimestamp;
        this.averagePersonCount = 0;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
    }
}
