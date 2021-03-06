package com.eventx.eventx;

/**
 * Created by Nishant on 3/9/2017.
 */

public class Event {
    private String category, description, image, name, venue;
    private long end_date_time;
    private long start_date_time;

    private String uid;
    private String state_category;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getState_category() {
        return state_category;
    }

    public void setState_category(String state_category) {
        this.state_category = state_category;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    private String state;

    public Event(){

    }

    public Event(String category, String description, String image, String name, long start_date_time, long end_date_time,String state) {
        this.category = category;
        this.description = description;
        this.image = image;
        this.state=state;
        this.name = name;
        this.start_date_time = start_date_time;
        this.end_date_time = end_date_time;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public long getEnd_date_time() {
        return end_date_time;
    }

    public void setEnd_date_time(long end_date_time) {
        this.end_date_time = end_date_time;
    }

    public long getStart_date_time() {
        return start_date_time;
    }

    public void setStart_date_time(long start_date_time) {
        this.start_date_time = start_date_time;
    }


}
