package com.qlog.qlogmobile.model;

public class Ticket {
    private String name, facility, purpose;
    private int queue_no;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getQueue_no() {
        return queue_no;
    }

    public void setQueue_no(int queue_no) {
        this.queue_no = queue_no;
    }
}
