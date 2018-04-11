/*
 * Copyright to Eduze@UoM 2017
 */

package org.augur.sense.api;

public enum State {
    STOPPED("STOPPED"),
    STOPPING("STOPPING"),
    STARTING("STARTING"),
    STARTED("STARTED");

    private String name;

    State(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
