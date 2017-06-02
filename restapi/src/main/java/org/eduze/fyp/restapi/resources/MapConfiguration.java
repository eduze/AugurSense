/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi.resources;

import org.eduze.fyp.core.util.PointMapping;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class MapConfiguration {

    private byte[] mapImage;
    private Map<Integer, PointMapping> mappings;

    public byte[] getMapImage() {
        return mapImage;
    }

    public void setMapImage(byte[] mapImage) {
        this.mapImage = mapImage;
    }

    public Map<Integer, PointMapping> getMappings() {
        return mappings;
    }

    public void setMappings(Map<Integer, PointMapping> mappings) {
        this.mappings = mappings;
    }
}
