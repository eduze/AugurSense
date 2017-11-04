package org.eduze.fyp.impl;

import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.api.resources.PersonLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PhotoMapper {
    public Map<Integer, List<PersonCoordinate>> map = new ConcurrentHashMap<>();

    public PhotoMapper(){

    }

    public void addSnapshot(PersonCoordinate personCoordinate, Set<Integer> ids){
        for(int id:ids){
            if(map.containsKey(id)){
                map.get(id).add(personCoordinate);
            }
            else{
                map.put(id,new ArrayList<>());
                map.get(id).add(personCoordinate);
            }
        }
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
