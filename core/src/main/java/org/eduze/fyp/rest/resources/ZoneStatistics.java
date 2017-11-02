package org.eduze.fyp.rest.resources;

import jdk.nashorn.internal.runtime.regexp.joni.constants.StringType;

public class ZoneStatistics {
    private double averagePersonCount;
    private long fromTimestamp;
    private long toTimestamp;

    private int zoneId;
    private String zoneName;

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
