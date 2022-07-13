package it.unipi.dii.iot.model;

public class RackSensor {
    private String rackSensorId;
    private String sensorType;
    private boolean alarm;

    public RackSensor(String rackSensorId, String sensorType, boolean alarm) {
        this.rackSensorId = rackSensorId;
        this.sensorType = sensorType;
        this.alarm = alarm;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getRackSensorId() {
        return rackSensorId;
    }

    public void setRackSensorId(String rackSensorId) {
        this.rackSensorId = rackSensorId;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }
}
