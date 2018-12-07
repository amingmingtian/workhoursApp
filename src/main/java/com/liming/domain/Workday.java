package com.liming.domain;

import com.liming.utils.Utils;

import java.util.Date;

public class Workday {
    private Date date;
    private Date ontime;
    private Date offtime;
    private String tag;
    private float workHours=0;
    private boolean isStatsFinished=false;

    public Workday(Date date, Date ontime, Date offtime) {
        this.date = date;
        this.ontime = ontime;
        this.offtime = offtime;
    }

    public String getTag() {
        if(!isStatsFinished){
            throw new RuntimeException("Error: Workday.getTag() Not Available until finish statistics");
        }
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public float getWorkHours() {
        if(!isStatsFinished){
            throw new RuntimeException("Error: Workday.getWorkHours() Not Available until finish statistics");
        }
        return workHours;
    }

    public void setWorkHours(float workHours) {
        this.workHours = workHours;
    }

    public Date getDate() {
        return date;
    }


    public Date getOntime() {
        return ontime;
    }


    public Date getOfftime() {
        return offtime;
    }

    public boolean isStatsFinished() {
        return isStatsFinished;
    }

    public void setStatsFinished(boolean statsFinished) {
        isStatsFinished = statsFinished;
    }

    @Override
    public String toString() {
        return  "[" +Utils.getDateY4Str(date)+"," +
                "ontime:" + (ontime == null ? null:Utils.getTimeStr(ontime)) +
                ", offtime:" + (offtime == null ? null:Utils.getTimeStr(offtime)) +
                ", workhours:" + String.format("%.2f", workHours) +
                ']';
    }

}
