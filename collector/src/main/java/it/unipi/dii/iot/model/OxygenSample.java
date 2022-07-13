package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class OxygenSample {
    private String id;
    private Timestamp timestamp;
    private float value;

    public OxygenSample(String id, Timestamp timestamp, int value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public float getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
