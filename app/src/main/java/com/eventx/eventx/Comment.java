package com.eventx.eventx;

/**
 * Created by Nishant on 4/25/2017.
 */

public class Comment {
    private String comment;
    private String profile_pic;
    private String username;
    private long time;

    private String user_id;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Comment(String comment, String profile_pic, String user_id, String username, long time) {
        this.comment = comment;
        this.profile_pic = profile_pic;
        this.username = username;
        this.time = time;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }


    public Comment(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
