package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class RackSample {
    private String rackSensorId;
    private Timestamp timestamp;
    private String measure;
    private int value;

    public RackSample(String rackSensorId, Timestamp timestamp, String measure, int value) {
        this.rackSensorId = rackSensorId;
        this.timestamp = timestamp;
        this.measure = measure;
        this.value = value;
    }

    public String getRackSensorId() {
        return rackSensorId;
    }

    public void setRackSensorId(String rackSensorId) {
        this.rackSensorId = rackSensorId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RackSample{" +
                "rackSensorId='" + rackSensorId + '\'' +
                ", timestamp=" + timestamp +
                ", measure='" + measure + '\'' +
                ", value=" + value +
                '}';
    }
}

