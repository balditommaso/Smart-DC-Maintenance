package it.unipi.dii.iot.model;

public class RackSensor {
    private String rackSensorId;
    private boolean alarm;
    private String type;

    public RackSensor(String rackSensorId, boolean alarm, String type) {
        this.rackSensorId = rackSensorId;
        this.alarm = alarm;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
