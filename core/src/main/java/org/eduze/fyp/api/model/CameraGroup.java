/*
 * <Paste your header here>
 */
package org.eduze.fyp.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@JsonIgnoreProperties({"cameraConfigs", "zones"})
public class CameraGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(unique = true)
    private String name;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] map;

    @OneToMany(mappedBy = "cameraGroup", cascade = {CascadeType.REMOVE})
    private Set<CameraConfig> cameraConfigs;

    @OneToMany(mappedBy = "cameraGroup", cascade = {CascadeType.REMOVE})
    private List<Zone> zones = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getMap() {
        return map;
    }

    public void setMap(byte[] map) {
        this.map = map;
    }

    public Set<CameraConfig> getCameraConfigs() {
        return cameraConfigs;
    }

    public void setCameraConfigs(Set<CameraConfig> cameraConfigs) {
        this.cameraConfigs = cameraConfigs;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }

    public String toString() {
        return String.format("%s", name);
    }
}
