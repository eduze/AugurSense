/*
 * Copyright (c) 2018 Augur Analytics
 */

/*
 * <Paste your header here>
 */
package org.eduze.fyp.core.reid;

public class GalleryComparisonResult {

    private GalleryPerson galleryPerson;
    private float confidence;

    public GalleryComparisonResult(GalleryPerson galleryPerson, float confidence) {
        this.galleryPerson = galleryPerson;
        this.confidence = confidence;
    }

    public GalleryPerson getGalleryPerson() {
        return galleryPerson;
    }

    public void setGalleryPerson(GalleryPerson galleryPerson) {
        this.galleryPerson = galleryPerson;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
