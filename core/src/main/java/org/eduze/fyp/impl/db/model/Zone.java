package org.eduze.fyp.impl.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "zones")
public class Zone {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String zoneName;

    private String xCoordinates;
    private String yCoordinates;

    public int getId() {
        return id;
    }

    public String getZoneName() {
        return zoneName;
    }

    public int[] getXCoordinates() {
        return getCoordinates(xCoordinates);
    }

    public int[] getYCoordinates() {
        return getCoordinates(yCoordinates);
    }

    private int[] getCoordinates(String coordinateField) {

        String[] strCoordinates = coordinateField.split(",");
        int[] coordinates = new int[strCoordinates.length];
        for (int i = 0; i < coordinates.length; i++)
            coordinates[i] = Integer.valueOf(strCoordinates[i].trim());
        return coordinates;
    }

    @Override
    public String toString() {
        return String.format("%s -> [%s and %s]", zoneName, xCoordinates, yCoordinates);
    }
}
