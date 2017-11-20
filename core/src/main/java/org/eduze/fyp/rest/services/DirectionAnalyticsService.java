package org.eduze.fyp.rest.services;

import org.eduze.fyp.api.resources.Point;
import org.eduze.fyp.impl.db.dao.CaptureStampDAO;
import org.eduze.fyp.impl.db.dao.PersonDAO;
import org.eduze.fyp.impl.db.model.Person;
import org.eduze.fyp.rest.resources.DirectionMap;

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
