package org.eduze.fyp.impl;

import org.eduze.fyp.api.resources.GlobalMap;
import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.api.resources.PersonLocation;
import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.impl.db.model.Person;
import org.eduze.fyp.rest.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PhotoMapper {
    public Map<Integer, List<PersonCoordinate>> map = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GlobalMap.class);

    private String photoSavePath = "";

    public String getPhotoSavePath() {
        return photoSavePath;
    }

    public void setPhotoSavePath(String photoSavePath) {
        this.photoSavePath = photoSavePath;
    }

    public PhotoMapper(){

    }

    private void saveSnapshot(PersonCoordinate pc, PersonSnapshot ps){
        try {
            new File(photoSavePath).mkdirs();

            BufferedImage view = ImageUtils.byteArrayToBufferedImage(pc.getImage());
            File outputfile = new File(photoSavePath + "/" + ps.getUuid() + ".png");
            ImageIO.write(view,"png", outputfile);
            logger.info("Saved " + outputfile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void addSnapshot(PersonCoordinate personCoordinate, Set<Integer> ids, PersonSnapshot ps){
        for(int id:ids){
            if(map.containsKey(id)){
                map.get(id).add(personCoordinate);
            }
            else{
                map.put(id,new ArrayList<>());
                map.get(id).add(personCoordinate);
            }
        }
        saveSnapshot(personCoordinate, ps);
    }

    public void removeSnapshots(Set<Integer> ids){
        for(Integer id: ids)
            this.map.remove(id);
    }

    public List<PersonCoordinate> getSnapshots(int id){
        if(!map.containsKey(id))
            return new ArrayList<>();
        return map.get(id);
    }
}
