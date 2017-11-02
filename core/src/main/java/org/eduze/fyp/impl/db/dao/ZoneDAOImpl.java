package org.eduze.fyp.impl.db.dao;

import org.eduze.fyp.impl.db.model.Person;
import org.eduze.fyp.impl.db.model.Zone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.TemporalType;
import java.util.List;

public class ZoneDAOImpl implements ZoneDAO {

    private static final Logger logger = LoggerFactory.getLogger(ZoneDAO.class);

    private SessionFactory sessionFactory;
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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
        List<Zone> zoneList = session.createQuery("from Zone").list();
        session.close();
        return zoneList;
    }

    @Override
    public void save(Zone zone) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(zone);
        tx.commit();
        session.close();
    }
}
