package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class HumiditySample {
    private String id;
    private Timestamp timestamp;
    private int value;

    public HumiditySample(String id, Timestamp timestamp, int value) {
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
