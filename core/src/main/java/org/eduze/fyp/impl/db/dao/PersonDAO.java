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

import java.util.Date;
import java.util.List;

public interface PersonDAO {

    void save(Person p);

    List<Person> getPersonFromTrackingId(int id);

    List<Person> list();

    Person getPerson(String uuid);

    List<Person> list(Date from, Date to);

    List<Integer> personIDs(Date from, Date to);
    List<Person> getRows(int id,Date from, Date to);

    List<Object[]> getZoneCounts(Date from, Date to);

    List<Object[]> getZoneStandCounts(Date from, Date to, double thresh);

    List<Object[]> getZoneSitCounts(Date from, Date to, double thresh);

    List<Object[]> getZoneUnclassifiedCounts(Date from, Date to, double threshSit, double threshStand);

    List<Object[]> getCrossCounts(Date from, Date to);


    List<Person[]> getZoneInflow(Date from, Date to, int zoneId, boolean useSegments);

    List<Person[]> getZoneOutflow(Date from, Date to, int zoneId, boolean useSegments);

    List<Object[]> getZonePersonCountVariation(Date from, Date to, String additionalCondition);

    List<Object[]> getTotalPersonCountVariation(Date from, Date to, String additionalCondition);

    List<Person> getZoneSwitchPersons(int ids, int segmentId, boolean useSegment);
    Person getTrackEnd(int ids, int segmentId, boolean useSegment);

}
