/*
 * <Paste your header here>
 */
package org.eduze.fyp.core.db.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class AbstractDAOImpl implements AbstractDAO {

    protected SessionFactory sessionFactory;

    @Override
    public void save(Object object) {
        Session session = this.sessionFactory.openSession();
        session.beginTransaction();
        session.persist(object);
        session.getTransaction().commit();
        session.close();
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
