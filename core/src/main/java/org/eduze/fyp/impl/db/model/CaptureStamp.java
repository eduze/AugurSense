package org.eduze.fyp.impl.db.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "capture_stamps")
public class CaptureStamp {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_stamp")
    private Date timestamp;

    public CaptureStamp(Date timestamp){
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}