/*
 * Copyright 2018 Eduze
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

/*
 * <Paste your header here>
 */
package org.eduze.fyp.core.db.dao;

import org.eduze.fyp.api.model.CameraConfig;
import org.eduze.fyp.api.model.CameraGroup;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CameraConfigDAOImpl extends AbstractDAOImpl implements CameraConfigDAO {

    private static final Logger logger = LoggerFactory.getLogger(CameraConfigDAO.class);


    @Override
    public List<CameraGroup> cameraGroups() {
        Session session = this.sessionFactory.openSession();
        List<CameraGroup> cameraGroups = session.createQuery("from CameraGroup", CameraGroup.class)
                .list();
        session.close();
        return cameraGroups;
    }

    @Override
    public void addCameraGroup(CameraGroup cameraGroup) {
        this.save(cameraGroup);
    }

    @Override
    public CameraConfig findById(int id) {
        Session session = this.sessionFactory.getCurrentSession();
        session.beginTransaction();
        CameraConfig cameraConfig = session.get(CameraConfig.class, id);
        session.getTransaction().commit();
        return cameraConfig;
    }

    @Override
    public CameraGroup findCameraGroupById(int id) {
        Session session = this.sessionFactory.getCurrentSession();
        if (!session.getTransaction().isActive()) {
            session.beginTransaction();
        }
        return session.get(CameraGroup.class, id);
    }

    @Override
    public CameraConfig findByCameraId(int cameraId) {
        Session session = this.sessionFactory.getCurrentSession();
        session.getTransaction();
        try {
            List<CameraConfig> cameraConfigs = session.createQuery("from CameraConfig where cameraId=:cameraId",
                    CameraConfig.class).setParameter("cameraId", cameraId).list();
            if (cameraConfigs.size() > 0) {
                return cameraConfigs.get(0);
            }
        } finally {
            session.getTransaction().commit();
        }
        return null;
    }

    @Override
    public CameraConfig findByCameraIpAndPort(String ipAndPort) {
        Session session = this.sessionFactory.getCurrentSession();
        session.beginTransaction();
        CameraConfig cameraConfig;
        try {
            cameraConfig = session.createQuery("from CameraConfig where ipAndPort=:ipAndPort", CameraConfig.class)
                    .setParameter("ipAndPort", ipAndPort).uniqueResult();
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.error("Error finding camera config by ip: {}", ipAndPort, e);
            throw new IllegalStateException("Error executing query", e);
        }

        return cameraConfig;
    }

    @Override
    public List<CameraConfig> list() {
        Session session = this.sessionFactory.openSession();
        List<CameraConfig> cameraConfigs = session.createQuery("from CameraConfig", CameraConfig.class)
                .list();
        session.close();
        return cameraConfigs;
    }

    @Override
    public void delete(CameraConfig cameraConfig) {
        Session session = this.sessionFactory.openSession();
        session.beginTransaction();
        try {
            session.delete(cameraConfig);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            logger.error("Error occurred when deleting camera config - {}", cameraConfig, e);
            throw new IllegalStateException("Unable to delete camera config", e);
        } finally {
            session.close();
        }
    }
}
