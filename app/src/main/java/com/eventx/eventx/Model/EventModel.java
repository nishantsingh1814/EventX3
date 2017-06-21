package com.eventx.eventx.Model;

/**
 * Created by Nishant on 5/10/2017.
 */

public class EventModel {
    private String id;
    private String title;
    private String city;
    private String description;
    private String img_url;
    private String[] cats;
    private Venue venue;
    private DateTime[] upcoming_occurrences;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String[] getCats() {
        return cats;
    }

    public void setCats(String[] cats) {
        this.cats = cats;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public DateTime[] getUpcoming_occurrences() {
        return upcoming_occurrences;
    }

    public void setUpcoming_occurrences(DateTime[] upcoming_occurrences) {
        this.upcoming_occurrences = upcoming_occurrences;
    }
}
