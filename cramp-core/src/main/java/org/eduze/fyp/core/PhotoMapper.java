package org.eduze.fyp.core;

import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.core.resources.GlobalMap;
import org.eduze.fyp.api.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PhotoMapper {
    public Map<Integer, List<PersonCoordinate>> map = new ConcurrentHashMap<>();

    public Map<Integer, List<PersonCoordinate>> unsavedPhotos = new ConcurrentHashMap<>(); //separate list kept in memory for storage purpose

    private static final Logger logger = LoggerFactory.getLogger(GlobalMap.class);

    private String photoSavePath = "";

    public String getPhotoSavePath() {
        return photoSavePath;
    }

    public void setPhotoSavePath(String photoSavePath) {
        this.photoSavePath = photoSavePath;
    }

    public PhotoMapper() {

    }

    public void onDBStore(PersonSnapshot ps) {
        for (int id : ps.getIds()) {
            if (unsavedPhotos.containsKey(id)) {
                List<PersonCoordinate> pcs = unsavedPhotos.remove(id);
                pcs.forEach(pc -> saveSnapshot(pc, ps));
            }
        }
    }

    private void saveSnapshot(PersonCoordinate pc, PersonSnapshot ps) {
        try {
            new File(photoSavePath).mkdirs();

            BufferedImage view = ImageUtils.byteArrayToBufferedImage(pc.getImage());
            File outputfile = new File(photoSavePath + "/" + ps.getUuid() + ".jpg"); // TODO: Wont this replace images of same person?
            ImageIO.write(view, "jpg", outputfile);
            pc.markSnapshotSaved();
            logger.info("Saved " + outputfile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addSnapshot(PersonCoordinate personCoordinate, Set<Integer> ids, PersonSnapshot ps) {
        for (int id : ids) {
            if (map.containsKey(id)) {
                map.get(id).add(personCoordinate);
            } else {
                map.put(id, new ArrayList<>());
                map.get(id).add(personCoordinate);
            }

            if (unsavedPhotos.containsKey(id)) {
                unsavedPhotos.get(id).add(personCoordinate);
            } else {
                unsavedPhotos.put(id, new ArrayList<>());
                unsavedPhotos.get(id).add(personCoordinate);
            }
        }

    }

    public void removeSnapshots(Set<Integer> ids) {
        for (Integer id : ids)
            this.map.remove(id);
    }

    public List<PersonCoordinate> getSnapshots(int id) {
        if (!map.containsKey(id))
            return new ArrayList<>();
        return map.get(id);
    }

    public List<PersonCoordinate> getLatestSnapshots() {
        final ArrayList<PersonCoordinate> results = new ArrayList<>();
        this.map.values().forEach((v) -> {results.add(v.get(0));});
        return results;
    }
}
