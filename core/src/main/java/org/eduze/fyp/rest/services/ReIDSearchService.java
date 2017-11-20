package org.eduze.fyp.rest.services;

import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.impl.PhotoMapper;
import org.eduze.fyp.impl.db.dao.PersonDAO;
import org.eduze.fyp.impl.db.model.Person;
import org.eduze.fyp.rest.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class ReIDSearchService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private PersonDAO personDAO;

    private List<Person> pendingSearchCandidates = null;

    public PersonDAO getPersonDAO() {
        return personDAO;
    }

    private PhotoMapper photoMapper = null;



    public void setPhotoMapper(PhotoMapper photoMapper) {
        this.photoMapper = photoMapper;
    }

    public PhotoMapper getPhotoMapper() {
        return photoMapper;
    }

    private Date fromDate = null;
    private Date toDate = null;
    private String candidateUUID = null;

    private boolean segmented = false;

    public ReIDSearchService(String reIdPath){
        try {
            setReIDPath(reIdPath);
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    private String reIdPath = "re_id/";

    private String getReIDPath(){
        return reIdPath;
    }

    public void setReIDPath(String re_id_path) {
        this.reIdPath = re_id_path;
        if(!this.reIdPath.endsWith("/"))
            this.reIdPath += "/";
    }

    public boolean verify(String uuid, Date fromDate, Date toDate, boolean segmented)
    {
        if(segmented != this.segmented)
            return false;

        if(!fromDate.equals(this.fromDate)) {
            logger.warn("Unknown fromDate");
            return false;
        }

        if(!toDate.equals(this.toDate)) {
            logger.warn("Unknown toDate");
            return false;
        }

        if(!candidateUUID.equals(this.candidateUUID)) {
            logger.warn("Unknown candiate uuid");
            return false;
        }
        return true;
    }

    private void init() throws IOException {
        File rootPath = new File(getReIDPath());
        rootPath.mkdirs();
        File communicatorRoot = new File(getReIDPath() + "close_communicator/");
        communicatorRoot.mkdirs();

        File doneFile = new File(getReIDPath() + "close_communicator/.done");
        if(doneFile.exists())
            doneFile.delete();

        File flagFile = new File(getReIDPath() + "close_communicator/flag_file.txt");
        if(flagFile.exists())
            flagFile.delete();
        flagFile.createNewFile();

        File galleryImagesFile = new File(getReIDPath() + "close_communicator/gallery_images.txt");
        if(galleryImagesFile.exists())
            galleryImagesFile.delete();
        galleryImagesFile.createNewFile();

        File probeImageFile = new File(getReIDPath() + "close_communicator/probe_image.txt");
        if(probeImageFile.exists())
            probeImageFile.delete();
        probeImageFile.createNewFile();

    }

    public List<PersonCoordinate> obtainSearchResults(String candidateUUID, Date fromDate, Date toDate, boolean segmented) throws IOException {
        if(!verify(candidateUUID,fromDate,toDate,segmented))
            return null;
        File doneFile = new File(getReIDPath() + "close_communicator/.done");
        if(!doneFile.exists())
           return null;

        File resultsFile = new File(getReIDPath() + "close_communicator/result.txt");
        if(!resultsFile.exists())
            return null;

        FileReader fileReader = new FileReader(resultsFile);
        Scanner sc = new Scanner(fileReader);
        String line = sc.nextLine();
        sc.close();
        fileReader.close();

        List<PersonCoordinate> personResults = new ArrayList<>();
        String[] results = line.split("\\s+");
        for(String result: results){
            Person person = pendingSearchCandidates.get(Math.max(0,Integer.valueOf(result.trim())-1));

            File f  = new File(photoMapper.getPhotoSavePath() + "/" + person.getUuid() + ".jpg");
            byte[] bytes = null;
            if(f.exists()){
                bytes = ImageUtils.bufferedImageToByteArray(ImageIO.read(f));
            }

            PersonCoordinate pc = new PersonCoordinate(person,bytes);

            personResults.add(pc);
        }

        return personResults;
    }

    public void invokeSearch(String candidateUUID, Date startDate, Date endDate, boolean segmented) throws IOException {
        File doneFile = new File(getReIDPath() + "close_communicator/.done");
        if(doneFile.exists())
            doneFile.delete();

        updateProbeFile(candidateUUID);
        int count = updateGalleryFile(startDate,endDate, segmented);

        File flagFile = new File(getReIDPath() + "close_communicator/flag_file.txt");
//        if(flagFile.exists())
//            flagFile.delete();

        flagFile.createNewFile();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(flagFile));
        PrintWriter printWriter = new PrintWriter(bufferedWriter);
        printWriter.printf("PENDING\n%d",count);
        printWriter.flush();
        bufferedWriter.flush();
        printWriter.close();
        bufferedWriter.close();

        this.fromDate = startDate;
        this.toDate = endDate;
        this.candidateUUID = candidateUUID;
        this.segmented = segmented;
    }

    public void updateProbeFile(String personUUID) throws IOException {
        File probeFile = new File(getReIDPath() + "close_communicator/probe_image.txt");
//        if(probeFile.exists())
//            probeFile.delete();
        probeFile.createNewFile();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(probeFile));
        PrintWriter printWriter = new PrintWriter(bufferedWriter);
        printWriter.print("gallery/" + personUUID + ".jpg");
        printWriter.flush();
        bufferedWriter.flush();
        printWriter.close();
        bufferedWriter.close();
    }

    public int updateGalleryFile(Date startDate, Date endDate, boolean segmented) throws IOException {
        List<Person> candidates = personDAO.list(startDate, endDate);

        this.pendingSearchCandidates =  new ArrayList<>();

        File galleryImagesFile = new File(getReIDPath() + "close_communicator/gallery_images.txt");
//        if(galleryImagesFile.exists())
//            galleryImagesFile.delete();
        galleryImagesFile.createNewFile();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(galleryImagesFile));
        PrintWriter printWriter = new PrintWriter(bufferedWriter);
        final boolean[] added = {false};
        final int[] addedCount = {0};

        HashMap<String,Integer> addedTracks = new HashMap<>();

        candidates.forEach((candidate)->{
            File galleryFile = new File(getReIDPath() + "gallery/" + candidate.getUuid() + ".jpg");
            if(galleryFile.exists())
            {
                //Filter multiple candidates from same track
                boolean canAdd = true;
                for(Integer id : candidate.getIds())
                {
                    if((!segmented && addedTracks.containsKey(String.valueOf(id))) || (segmented && addedTracks.containsKey(String.valueOf(id) + "_" + candidate.getTrackSegmentIndex()))){
                        if((!segmented && addedTracks.get(String.valueOf(id)) > 3) || (segmented && addedTracks.get(String.valueOf(id) + "_"+ candidate.getTrackSegmentIndex()) > 3))
                        {
                            canAdd = false;
                        }
                        if(segmented)
                        {
                            addedTracks.put(id +"_" + candidate.getTrackSegmentIndex(),addedTracks.get(id + "_" + candidate.getTrackSegmentIndex())+1);
                        }
                        else{
                            addedTracks.put(String.valueOf(id),addedTracks.get(String.valueOf(id))+1);
                        }

                    }
                    else{
                        if(segmented)
                        {
                            addedTracks.put(String.valueOf(id) + "_" + candidate.getTrackSegmentIndex(),1);
                        }
                        else{
                            addedTracks.put(String.valueOf(id),1);
                        }

                    }
                }
                if(canAdd)
                {
                    if(added[0])
                        printWriter.println();

                    this.pendingSearchCandidates.add(candidate);
                    printWriter.print("gallery/" + candidate.getUuid() + ".jpg");
                    added[0] = true;
                    addedCount[0]++;
                }


            }

        });
        printWriter.flush();
        bufferedWriter.flush();
        printWriter.close();
        bufferedWriter.close();

        return addedCount[0];
    }
}
