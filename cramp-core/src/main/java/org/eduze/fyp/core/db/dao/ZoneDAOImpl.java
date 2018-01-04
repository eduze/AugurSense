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

package org.eduze.fyp.core.db.dao;

import org.eduze.fyp.api.model.Zone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ZoneDAOImpl implements ZoneDAO {

    private static final Logger logger = LoggerFactory.getLogger(ZoneDAO.class);

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Zone findById(int zoneId) {
        Session session = this.sessionFactory.openSession();
        Zone zone = session.load(Zone.class, zoneId);
        session.close();
        return zone;
    }

    @Override
    public Zone getZone(String zoneName) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("from Zone Z where Z.name = :name")
                .setParameter("name", zoneName, StringType.INSTANCE);
        List zoneList = query.list();
        session.close();
        return (Zone) zoneList.get(0);
    }

    @Override
    public List<Zone> list() {
        Session session = this.sessionFactory.openSession();
        List<Zone> zoneList = (List<Zone>) session.createQuery("from Zone").list();
        session.close();
        return zoneList;
    }

    @Override
    public void save(Zone zone) {
        Session session = this.sessionFactory.openSession();
        session.persist(zone);
        session.close();
    }

    @Override
    public void update(Zone zone) {
        Session session = this.sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.update(zone);
        transaction.commit();
        session.close();
    }

    @Override
    public void delete(int zoneId) {
        Session session = this.sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Zone zone = session.load(Zone.class, zoneId);
            if (zone != null) {
                session.delete(zone);
                logger.debug("Deleted zone - {}", zoneId);
            } else {
                logger.warn("No zone found for id - {}", zoneId);
            }
        } catch (Exception e) {
            logger.error("Error occurred when deleting zone - {}, {}", zoneId, e);
            transaction.rollback();
        }
        transaction.commit();
        session.close();
    }
}
