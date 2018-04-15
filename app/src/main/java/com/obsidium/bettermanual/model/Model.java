package com.obsidium.bettermanual.model;

public interface Model {
    void setValue(int i);
    String getValue();
    boolean isSupported();
    void setListner(Events events);

    public interface Events
    {
        void onValueChanged();
        void onIsSupportedChanged();
    }
}
