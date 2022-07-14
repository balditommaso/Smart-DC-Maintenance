package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class HumiditySample {
    private String id;
    private int value;

    public HumiditySample(String id, int value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
