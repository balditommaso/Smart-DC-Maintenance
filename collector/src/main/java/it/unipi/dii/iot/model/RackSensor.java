package it.unipi.dii.iot.model;

public class RackSensor {
    private String rackSensorId;
    private boolean alarm;

    public RackSensor(String rackSensorId, boolean alarm) {
        this.rackSensorId = rackSensorId;
        this.alarm = alarm;
    }

    public String getRackSensorId() {
        return rackSensorId;
    }

    public void setRackSensorId(String rackSensorId) {
        this.rackSensorId = rackSensorId;
    }

    public boolean getAlarm() {
        return alarm;
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }

    @Override
    public String toString() {
        return "RackSensor{" +
                "rackSensorId='" + rackSensorId + '\'' +
                ", alarm=" + alarm +
                '}';
    }
}
