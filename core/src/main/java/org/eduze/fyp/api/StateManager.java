/*
 * Copyright to Eduze@UoM 2017
 */

package org.eduze.fyp.api;

import java.util.stream.Stream;

/**
 * A utility class for managing states of individual components inside the {@link AnalyticsEngine}
 *
 * @author Imesha Sudasingha
 */
public final class StateManager {

    private State state;

    public StateManager(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        this.state = state;
    }

    public synchronized void setState(State newState) {
        state = newState;
    }

    public synchronized void checkState(State... states) {
        if (states != null && Stream.of(states).filter(s -> this.state.equals(s)).count() == 0) {
            throw new IllegalStateException("System is at state: " + state);
        }
    }
}
