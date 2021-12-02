package com.qlog.qlogmobile.model;

public class Purpose {
    private String title;
    private boolean isSelected;

    public Purpose(String title, boolean b) {
        this.title = title;
        this.isSelected = b;
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
}
