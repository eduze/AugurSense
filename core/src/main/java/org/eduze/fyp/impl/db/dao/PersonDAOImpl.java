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

package org.eduze.fyp.impl.db.dao;

import org.eduze.fyp.impl.db.model.Person;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.DoubleType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.TemporalType;
import java.util.*;

public class PersonDAOImpl implements PersonDAO {

    private static final Logger logger = LoggerFactory.getLogger(PersonDAO.class);

    private SessionFactory sessionFactory;

    @Override
    public void save(Person p) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(p);
        tx.commit();
        session.close();
    }

    @Override
    public List<Person> getPersonFromTrackingId(int id) {
        String ids = String.valueOf(id);
        Session session = this.sessionFactory.openSession();
        List<Person> personList = session.createQuery("from Person P where P.ids=:ids").setParameter("ids",ids, StringType.INSTANCE).list();
        session.close();
        return personList;
    }

    @Override
    public List<Person> list() {
        Session session = this.sessionFactory.openSession();
        List<Person> personList = session.createQuery("from Person").list();
        session.close();
        return personList;
    }

    @Override
    public Person getPerson(String uuid) {
        Session session = this.sessionFactory.openSession();
        List<Person> personList = session.createQuery("from Person P where P.uuid=:uuid").setParameter("uuid",uuid,StringType.INSTANCE).list();
        session.close();
        return personList.get(0);
    }

    @Override
    public List<Person> list(Date from, Date to) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("from Person P where P.timestamp between :startTime and :endTime order by P.id")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP);
        List personList = query.list();
        session.close();
        return personList;
    }

    @Override
    public List<Integer> personIDs(Date from, Date to) {
        List<Integer> ids = new ArrayList<>();
        //TODO: neeed to Implement
        return ids;
    }

    @Override
    public List<Person> getRows(int id, Date from, Date to) {
//        Set<Integer> i = new HashSet<>();
//        i.add(id);
//        i.add(5);
//        Session session = this.sessionFactory.openSession();
//        Query query = session.createQuery("from Person P where P.ids = :id  and P.timestamp between :startTime and :endTime ")
//                .setParameter("startTime", from, TemporalType.TIMESTAMP)
//                .setParameter("endTime", to, TemporalType.TIMESTAMP)
//                .setParameter("id", i);
//        List personList = query.list();
//        session.close();
//        return personList;
        return null;

    }


    @Override
    public List<Object[]> getZoneCounts(Date from, Date to) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("select P.instantZoneId, count(*) from Person P where P.timestamp between :startTime and :endTime group by P.instantZoneId")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP);
        List zoneCountList = query.list();
        session.close();
        return zoneCountList;
    }

    @Override
    public List<Object[]> getZoneStandCounts(Date from, Date to, double thresh) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("select P.instantZoneId, count(*) from Person P where P.timestamp between :startTime and :endTime and P.standProbability >= :thresh group by P.instantZoneId")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP)
                .setParameter("thresh",thresh, DoubleType.INSTANCE);
        List zoneCountList = query.list();
        session.close();
        return zoneCountList;
    }

    @Override
    public List<Object[]> getZoneSitCounts(Date from, Date to, double thresh) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("select P.instantZoneId, count(*) from Person P where P.timestamp between :startTime and :endTime and P.sitProbability >= :thresh group by P.instantZoneId")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP)
                .setParameter("thresh",thresh,DoubleType.INSTANCE);
        List zoneCountList = query.list();
        session.close();
        return zoneCountList;
    }

    @Override
    public List<Object[]> getZoneUnclassifiedCounts(Date from, Date to, double threshSit, double threshStand) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("select P.instantZoneId, count(*) from Person P where P.timestamp between :startTime and :endTime and P.sitProbability < :threshSit and P.standProbability < :threshStand group by P.instantZoneId")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP)
                .setParameter("threshSit",threshSit,DoubleType.INSTANCE)
                .setParameter("threshStand", threshStand, DoubleType.INSTANCE);
        List zoneCountList = query.list();
        session.close();
        return zoneCountList;
    }

    @Override
    public List<Object[]> getCrossCounts(Date from, Date to) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("select z1.id as pastZone, z2.id as currentZone, count(p.id) from Person p join Zone z1 on p.pastPersistantZoneId = z1 join Zone z2 on p.persistantZoneId = z2.id where p.timestamp between :startTime and :endTime group by z1.id, z2.id")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP);
//        Query query = session.createQuery("select p.pastPersistantZoneId as pastZone, p.persistantZoneId as currentZone, count(p.id) from Person p  where p.timestamp between :startTime and :endTime group by p.pastPersistantZoneId, p.persistantZoneId")
//                .setParameter("startTime", from, TemporalType.TIMESTAMP)
//                .setParameter("endTime", to, TemporalType.TIMESTAMP);

        List crossList = query.list();
        session.close();
        return crossList;
    }

    @Override
    public List<Object[]> getPersonCountVariation(Date from, Date to, String additionalVariation) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("select P.timestamp, P.instantZoneId, count(P.uuid) as p_count, (select count(T.timestamp) from CaptureStamp T where T.timestamp = P.timestamp) as t_count from Person P where P.timestamp between :startTime and :endTime " + additionalVariation + " group by P.instantZoneId, P.timestamp")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP);
//        Query query = session.createQuery("select p.pastPersistantZoneId as pastZone, p.persistantZoneId as currentZone, count(p.id) from Person p  where p.timestamp between :startTime and :endTime group by p.pastPersistantZoneId, p.persistantZoneId")
//                .setParameter("startTime", from, TemporalType.TIMESTAMP)
//                .setParameter("endTime", to, TemporalType.TIMESTAMP);

        List crossList = query.list();
        session.close();
        return crossList;
    }


    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
