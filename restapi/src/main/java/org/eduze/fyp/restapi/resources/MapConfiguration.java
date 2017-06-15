/*
 * Copyright to Eduze@UoM 2017
 */
package org.eduze.fyp.restapi.resources;

import org.eduze.fyp.core.api.resources.PointMapping;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MapConfiguration extends Status {

    private byte[] mapImage;
    private PointMapping mapping;
    private int mapWidth;
    private int mapHeight;

    public MapConfiguration() {
        super(true);
    }

    public byte[] getMapImage() {
        return mapImage;
    }

    public void setMapImage(byte[] mapImage) {
        this.mapImage = mapImage;
    }

    public PointMapping getMapping() {
        return mapping;
    }

    public void setMapping(PointMapping mapping) {
        this.mapping = mapping;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }
}
