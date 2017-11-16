/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.eduze.fyp.impl.db.model;

import org.eduze.fyp.impl.db.helpers.PersonIdConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "persons")
public class Person {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_stamp")
    private Date timestamp;

    private int trackSegmentIndex;



    private String uuid;
    private String previousUuid;

    private double x;
    private double y;
    private int instantZoneId;

    private int persistantZoneId;
    private int pastPersistantZoneId;

    private double sitProbability;
    private double standProbability;

    private double headDirectionX;
    private double headDirectionY;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPreviousUuid() {
        return previousUuid;
    }

    public void setPreviousUuid(String previousUuid) {
        this.previousUuid = previousUuid;
    }

    public void setHeadDirectionY(double headDirectionY) {
        this.headDirectionY = headDirectionY;
    }

    public void setHeadDirectionX(double headDirectionX) {
        this.headDirectionX = headDirectionX;
    }

    public double getHeadDirectionY() {
        return headDirectionY;
    }

    public void setStandProbability(double standProbability) {
        this.standProbability = standProbability;
    }

    public void setSitProbability(double sitProbability) {
        this.sitProbability = sitProbability;
    }

    public double getStandProbability() {
        return standProbability;
    }

    public double getSitProbability() {
        return sitProbability;
    }

    public double getHeadDirectionX() {
        return headDirectionX;
    }

    public String getUuid() {
        return uuid;
    }

    @Convert(converter = PersonIdConverter.class)
    private Set<Integer> ids;

    protected Person() {
    }

    public Person(Set<Integer> ids, long timestamp, double x, double y,double sitProbability, double standProbability, double headDirectionX, double headDirectionY, int instantZoneId, int persistantZoneId, int pastPersistantZoneId, String uuid, String previousUuid, int trackSegmentIndex) {
        setIds(ids);
        this.timestamp = new Date(timestamp);
        this.x = x;
        this.y = y;
        this.instantZoneId = instantZoneId;
        this.persistantZoneId = persistantZoneId;
        this.pastPersistantZoneId = pastPersistantZoneId;

        this.sitProbability = sitProbability;
        this.standProbability = standProbability;
        this.headDirectionX = headDirectionX;
        this.headDirectionY = headDirectionY;
        this.uuid = uuid;
        this.trackSegmentIndex = trackSegmentIndex;
        this.previousUuid = previousUuid;
    }

    public int getInstantZoneId(){return instantZoneId;}

    public void setInstantZoneId(int zoneId) {
        this.instantZoneId = zoneId;
    }

    public int getId() {
        return id;
    }

    public int getPastPersistantZoneId() {
        return pastPersistantZoneId;
    }

    public int getPersistantZoneId() {
        return persistantZoneId;
    }

    public void setPastPersistantZoneId(int pastPersistantZoneId) {
        this.pastPersistantZoneId = pastPersistantZoneId;
    }

    public void setPersistantZoneId(int persistantZoneId) {
        this.persistantZoneId = persistantZoneId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Set<Integer> getIds() {
        return ids;
    }

    public void setIds(Set<Integer> ids) {
        this.ids = new HashSet<>(ids);
    }

    public int getTrackSegmentIndex() {
        return trackSegmentIndex;
    }

    public void setTrackSegmentIndex(int trackSegmentIndex) {
        this.trackSegmentIndex = trackSegmentIndex;
    }
}


