/*
 * Copyright to Eduze@UoM 2017
 */

package org.augur.sense.api;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A utility class for managing states of individual components inside the {@link AnalyticsEngine}
 *
 * @author Imesha Sudasingha
 */
public final class StateManager {

    private State state;
    private Map<State, Object> waiters = new HashMap<>();

    public StateManager(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        this.state = state;
    }

    public synchronized void setState(State newState) {
        state = newState;
        synchronized (this) {
            waiters.computeIfAbsent(state, k -> new Object());
        }

        synchronized (waiters.get(state)) {
            waiters.get(state).notify();
        }
    }

    public synchronized void checkState(State... states) {
        if (states != null && Stream.of(states).filter(s -> this.state.equals(s)).count() == 0) {
            throw new IllegalStateException("System is at state: " + state);
        }
    }

    public synchronized boolean isState(State state) {
        return this.state.equals(state);
    }

    public void waitFor(State state) throws InterruptedException {
        if (state != null && !this.state.equals(state)) {
            synchronized (this) {
                waiters.computeIfAbsent(state, k -> new Object());
            }

            synchronized (waiters.get(state)) {
                waiters.get(state).wait();
            }
        }
    }
}
