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

package org.augur.sense.core.db.dao;

import org.augur.sense.api.model.CaptureStamp;
import org.augur.sense.api.model.CaptureStamp;
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
