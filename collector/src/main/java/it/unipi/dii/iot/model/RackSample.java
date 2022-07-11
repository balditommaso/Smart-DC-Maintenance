package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class RackSample {
    private String rackSensorId;
    private Timestamp timestamp;
    private int temperature;
    private int humidity;

    public RackSample(String rackSensorId, Timestamp timestamp, int temperature, int humidity) {
        this.rackSensorId = rackSensorId;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
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

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
}
