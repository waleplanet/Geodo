package com.app.waleent.geodo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

/**
 * Created by wale on 10/30/17.
 */

@Entity
public class TaskModel {

    @PrimaryKey(autoGenerate = true)
    public int id;
    private String title;
    private String location;
    private String latitude;
    private String longitude;
    public Boolean completed;

    @TypeConverters(DateConverter.class)
    private Date createdDate;

    public TaskModel (String title, String location, Date createdDate, String latitude, String longitude){
        this.title = title;
        this.location = location;
        this.createdDate = createdDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.completed = false;
    }
    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setCompleted(boolean bool) {
        completed = bool;
    }

    public Boolean getCompleted() {
        return completed;
    }
}
