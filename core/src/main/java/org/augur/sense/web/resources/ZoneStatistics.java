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

import java.util.Map;

public class ZoneStatistics {
    private double averagePersonCount;
    private double averageSittingCount;
    private double averageStandingCount;
    private double averageUnclassifiedPoseCount;

    private Map<Long, Double> totalCountVariation;
    private Map<Long, Double> totalStandingCountVariation;
    private Map<Long, Double> totalSittingCountVariation;

    private long fromTimestamp;
    private long toTimestamp;

    private Map<Integer, Long> outgoingMap = null;
    private Map<Integer, Long> incomingMap = null;

    private int zoneId;
    private String zoneName;

    private long totalOutgoing = 0;
    private long totalIncoming = 0;

    public ZoneStatistics(int zoneId, String zoneName, long fromTimestamp, long toTimestamp) {
        this.fromTimestamp = fromTimestamp;
        this.toTimestamp = toTimestamp;
        this.averagePersonCount = 0;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
    }

    public Map<Long, Double> getTotalCountVariation() {
        return totalCountVariation;
    }

    public void setTotalCountVariation(Map<Long, Double> totalCountVariation) {
        this.totalCountVariation = totalCountVariation;
    }

    public Map<Long, Double> getTotalSittingCountVariation() {
        return totalSittingCountVariation;
    }

    public Map<Long, Double> getTotalStandingCountVariation() {
        return totalStandingCountVariation;
    }

    public void setTotalSittingCountVariation(Map<Long, Double> totalSittingCountVariation) {
        this.totalSittingCountVariation = totalSittingCountVariation;
    }

    public void setTotalStandingCountVariation(Map<Long, Double> totalStandingCountVariation) {
        this.totalStandingCountVariation = totalStandingCountVariation;
    }

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
}
