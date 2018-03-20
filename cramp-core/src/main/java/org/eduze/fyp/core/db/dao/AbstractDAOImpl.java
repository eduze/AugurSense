/*
 * <Paste your header here>
 */
package org.eduze.fyp.core.db.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractDAOImpl implements AbstractDAO {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDAOImpl.class);

    SessionFactory sessionFactory;

    @Override
    public void save(Object object) {
        Session session = this.sessionFactory.openSession();
        session.beginTransaction();
        try {
            session.persist(object);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new IllegalStateException("Unable to save", e);
        }
    }

    @Override
    public void update(Object object) {
        Session session = this.sessionFactory.openSession();
        session.beginTransaction();
        try {
            session.update(object);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.error("Error occurred when updating - {}", object, e);
            throw new IllegalStateException("Unable to update", e);
        }
    }

    @Override
    public Object merge(Object object) {
        Session session = this.sessionFactory.openSession();
        session.beginTransaction();
        Object persisted;
        try {
            persisted = session.merge(object);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.error("Error occurred when updating - {}", object, e);
            throw new IllegalStateException("Unable to update", e);
        }
        return persisted;
    }

    @Override
    public void persist(Object object) {
        Session session = this.sessionFactory.openSession();
        session.beginTransaction();
        try {
            session.persist(object);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.error("Unable to persist", e);
            throw new IllegalStateException("Unable to persist", e);
        }
    }

    @Override
    public void saveOrUpdate(Object object) {
        Session session = this.sessionFactory.getCurrentSession();
        session.beginTransaction();
        try {
            session.saveOrUpdate(object);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.error("Unable to save or update", e);
            throw new IllegalStateException("Unable to save or update", e);
        }
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
