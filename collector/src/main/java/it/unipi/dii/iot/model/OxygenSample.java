package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class OxygenSample {
    private String id;
    private float value;

    public OxygenSample(String id, float value) {
        this.id = id;
        this.value =  value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = (float) value/10;
    }
}
