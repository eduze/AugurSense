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

package org.eduze.fyp.api.resources;

import org.eduze.fyp.api.model.Person;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement
public class PersonCoordinate extends Coordinate {

    private byte[] image;

    private String uuid;

    private Set<Integer> ids;

    private int trackSegmentIndex;

    public int getTrackSegmentIndex() {
        return trackSegmentIndex;
    }

    public void setTrackSegmentIndex(int trackSegmentIndex) {
        this.trackSegmentIndex = trackSegmentIndex;
    }

    public PersonCoordinate() {
    }

    public PersonCoordinate(Person p, byte[] image) {
        super(p.getX(), p.getY(), p.getTimestamp().getTime(), p.getStandProbability(), p.getSitProbability(),
                p.getHeadDirectionX(), p.getHeadDirectionY());
        this.image = image;
        this.uuid = p.getUuid();
        this.ids = p.getIds();
        this.trackSegmentIndex = p.getTrackSegmentIndex();
    }

    public PersonCoordinate(double x, double y, long timestamp, double sitProbability, double standProbability,
            double headDirectionX, double headDirectionY, byte[] image) {
        super(x, y, timestamp, standProbability, sitProbability, headDirectionX, headDirectionY);
        this.image = image;
    }

    public boolean isSnapshotSaved() {
        return snapshotSaved;
    }

    public void markSnapshotSaved() {
        this.snapshotSaved = true;
    }

    private boolean snapshotSaved = false;


    public void setIds(Set<Integer> ids) {
        this.ids = ids;
    }

    public Set<Integer> getIds() {
        return ids;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Coordinate toCoordinate() {
        return new Coordinate(getX(), getY(), getTimestamp(), getSitProbability(), getStandProbability(), getHeadDirectionX(), getHeadDirectionY());
    }
}
