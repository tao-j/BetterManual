package com.obsidium.bettermanual.model;

public abstract class AbstractModel implements Model {

    protected Events eventsListner;
    protected boolean supported;
    protected String value;
    protected String[] values;
    protected int value_int;

    @Override
    public void setListner(Events events) {
        this.eventsListner = events;
    }
}
