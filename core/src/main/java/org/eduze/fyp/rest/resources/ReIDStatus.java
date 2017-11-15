package org.eduze.fyp.rest.resources;

import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.impl.db.model.Person;

import java.util.List;

public class ReIDStatus {
    private List<PersonCoordinate> results;

    private boolean isPending;
    private boolean isCompleted;

    private boolean isInvalid;

    public boolean isInvalid() {
        return isInvalid;
    }

    public void setInvalid(boolean invalid) {
        isInvalid = invalid;
    }
    public ReIDStatus(List<PersonCoordinate> results, boolean isPending, boolean isCompleted, boolean isInvalid)
    {
        this.results = results;
        this.isCompleted = isCompleted;
        this.isPending = isPending;
        this.isInvalid = isInvalid;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public List<PersonCoordinate> getResults() {
        return results;
    }

    public void setResults(List<PersonCoordinate> results) {
        this.results = results;
    }
}
