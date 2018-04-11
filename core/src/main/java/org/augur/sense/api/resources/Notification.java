/*
 * <Paste your header here>
 */
package org.augur.sense.api.resources;

public class Notification {

    private String title;
    private String message;
    private long timestamp;

    public Notification() {
    }

    public Notification(String title, String message, long timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Notification(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
