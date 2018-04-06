/*
 * <Paste your header here>
 */
package org.eduze.fyp.api;

import org.eduze.fyp.api.model.CameraGroup;
import org.eduze.fyp.api.resources.Notification;
import org.eduze.fyp.api.resources.PersonSnapshot;

import java.util.List;

/**
 * Represents a condition to be evaluated/checked for generating notifications.
 *
 * @author Imesha Sudasingha
 */
public interface Condition {

    /**
     * Evaluates a given logic and determines whether any notification requires to be sent
     *
     * @return list of notifications to be sent | not null
     */
    List<Notification> evaluate(CameraGroup cameraGroup, List<List<PersonSnapshot>> snapshots);
}
