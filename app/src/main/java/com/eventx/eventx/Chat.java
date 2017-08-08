package com.eventx.eventx;

/**
 * Created by Nishant on 6/21/2017.
 */

public class Chat {
    public String message;

    public String username;

    public Chat() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public Chat(String message, String username) {
        this.message = message;
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
