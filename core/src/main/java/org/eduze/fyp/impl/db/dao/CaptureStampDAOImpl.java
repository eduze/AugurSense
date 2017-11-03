package org.eduze.fyp.impl.db.dao;

import org.eduze.fyp.impl.db.model.CaptureStamp;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

public class CaptureStampDAOImpl implements CaptureStampDAO {

    private static final Logger logger = LoggerFactory.getLogger(PersonDAO.class);

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(CaptureStamp captureStamp) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(captureStamp);
        tx.commit();
        session.close();
    }

    @Override
    public List<Object[]> getCaptureStamps(Date from, Date to) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("from CaptureStamp CP where CP.timestamp between :startTime and :endTime")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP);
        List captureStampList = query.list();
        session.close();
        return captureStampList;
    }

    @Override
    public long getCaptureStampCount(Date from, Date to) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("select count(*) from CaptureStamp CP where CP.timestamp between :startTime and :endTime")
                .setParameter("startTime", from, TemporalType.TIMESTAMP)
                .setParameter("endTime", to, TemporalType.TIMESTAMP);
        List captureStampList = query.list();
        session.close();
        return (long) captureStampList.get(0);
    }
}
