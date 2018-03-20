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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;

@XmlRootElement
@Entity
@JsonIgnoreProperties("cameraConfigs")
public class CameraGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(unique = true)
    private String name;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] map;

    @XmlTransient
    @OneToMany(mappedBy = "cameraGroup", cascade = {CascadeType.REMOVE})
    private Set<CameraConfig> cameraConfigs;

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

    public String toString() {
        return String.format("%s", name);
    }
}
