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

package org.eduze.fyp.api.model;

import javax.persistence.*;
import java.util.Date;

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

    private double x;
    private double y;

    @ManyToOne
    private Zone instantZone;
    @ManyToOne
    private Zone persistentZone;
    @ManyToOne
    private Zone pastPersistentZone;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    @Column(nullable = false)
    private int personId;

    private double sitProbability;
    private double standProbability;

    private double headDirectionX;
    private double headDirectionY;

    protected Person() { }

    public Person(Date timestamp, double x, double y, Zone instantZone, Zone persistentZone, Zone pastPersistentZone,
            byte[] image, int personId, double sitProbability, double standProbability, double headDirectionX, double headDirectionY) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.instantZone = instantZone;
        this.persistentZone = persistentZone;
        this.pastPersistentZone = pastPersistentZone;
        this.image = image;
        this.personId = personId;
        this.sitProbability = sitProbability;
        this.standProbability = standProbability;
        this.headDirectionX = headDirectionX;
        this.headDirectionY = headDirectionY;
    }

    public int getId() {
        return id;
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

    public Zone getInstantZone() {
        return instantZone;
    }

    public void setInstantZone(Zone instantZone) {
        this.instantZone = instantZone;
    }

    public Zone getPersistentZone() {
        return persistentZone;
    }

    public void setPersistentZone(Zone persistentZone) {
        this.persistentZone = persistentZone;
    }

    public Zone getPastPersistentZone() {
        return pastPersistentZone;
    }

    public void setPastPersistentZone(Zone pastPersistentZone) {
        this.pastPersistentZone = pastPersistentZone;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }
}


