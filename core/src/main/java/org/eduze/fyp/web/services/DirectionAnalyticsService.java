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

package org.eduze.fyp.web.services;

import org.eduze.fyp.api.resources.Point;
import org.eduze.fyp.core.db.dao.CaptureStampDAO;
import org.eduze.fyp.core.db.dao.PersonDAO;
import org.eduze.fyp.api.model.Person;
import org.eduze.fyp.web.resources.DirectionMap;

import java.util.Date;
import java.util.List;

public class DirectionAnalyticsService {
    private PersonDAO personDAO;
    private CaptureStampDAO captureStampDAO;

    public DirectionAnalyticsService(PersonDAO personDAO, CaptureStampDAO captureStampDAO){
        this.personDAO = personDAO;
        this.captureStampDAO = captureStampDAO;
    }

    public CaptureStampDAO getCaptureStampDAO() {
        return captureStampDAO;
    }

    public void setCaptureStampDAO(CaptureStampDAO captureStampDAO) {
        this.captureStampDAO = captureStampDAO;
    }

    public PersonDAO getPersonDAO() {
        return personDAO;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    public DirectionMap getDirectionAnalytics(Date start, Date end, int cellSize, int directionCount){
        DirectionMap results = new DirectionMap(cellSize,directionCount);
        List<Object[]> pairs = personDAO.getTrackPairs(start,end);
        pairs.forEach((pair)->{
            Person p1 = (Person) pair[0];
            Person p2 = (Person) pair[1];
            double cx = p2.getX() - p1.getX();
            double cy = p2.getY() - p1.getY();
            double distance = Math.sqrt(cx*cx + cy*cy);
            //todo: do proper time division
            long timeDif = p2.getTimestamp().getTime() - p1.getTimestamp().getTime();
            double speed = distance;
            if(timeDif != 0)
                speed /= distance;
            results.appendEdge(new Point(p1.getX(),p1.getY()),new Point(p2.getX(),p2.getY()),1,speed, new Point(p1.getHeadDirectionX(),p2.getHeadDirectionY()));
        });

        results.normalize();
        return results;

    }

}
