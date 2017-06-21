package com.eventx.eventx;

/**
 * Created by Nishant on 5/2/2017.
 */

public class Notification {
    private String e_id;
    private String e_name;
    private String u_name;

    public Notification(String e_id, String e_name, String u_name) {
        this.e_id = e_id;
        this.e_name = e_name;
        this.u_name = u_name;
    }
    public Notification(){

    }

    public String getE_id() {

        return e_id;
    }

    public void setE_id(String e_id) {
        this.e_id = e_id;
    }

    public String getE_name() {
        return e_name;
    }

    public void setE_name(String e_name) {
        this.e_name = e_name;
    }

    public String getU_name() {
        return u_name;
    }

    public void setU_name(String u_name) {
        this.u_name = u_name;
    }
}
