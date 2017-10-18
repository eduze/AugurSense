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
    public List<Person> list() {
        Session session = this.sessionFactory.openSession();
        List<Person> personList = session.createQuery("from Person").list();
        session.close();
        return personList;
    }

    @Override
    public List<Person> list(Date from, Date to) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("from Person P where P.timestamp between :startTime and :endTime")
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

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
