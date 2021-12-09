package com.qlog.qlogmobile.model;

import java.util.ArrayList;

public class Purpose {
    private int id;
    private String title;
    private boolean isSelected, hasRequiredFacility;
    private ArrayList<Facility> facility;

    public Purpose(String title, boolean isSelected) {
        this.title = title;
        this.isSelected = isSelected;
    }

    public Purpose() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean hasRequiredFacility() {
        return hasRequiredFacility;
    }

    public void setHasRequiredFacility(boolean hasRequiredFacility) {
        this.hasRequiredFacility = hasRequiredFacility;
    }

    public ArrayList<Facility> getFacility() {
        return facility;
    }

    public void setFacility(ArrayList<Facility> facility) {
        this.facility = facility;
    }
}
