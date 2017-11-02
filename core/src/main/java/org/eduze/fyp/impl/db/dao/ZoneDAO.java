package org.eduze.fyp.impl.db.dao;

import org.eduze.fyp.impl.db.model.Zone;

import java.util.List;

public interface ZoneDAO {
    Zone getZone(String zoneName);
    List<Zone> list();
    void save(Zone zone);
}
