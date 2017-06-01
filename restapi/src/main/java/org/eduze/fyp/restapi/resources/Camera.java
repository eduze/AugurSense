package org.eduze.fyp.restapi.resources;

public class Camera {

    private int id;

    public Camera() {
    }

    public Camera(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return String.valueOf(id);
    }
}
