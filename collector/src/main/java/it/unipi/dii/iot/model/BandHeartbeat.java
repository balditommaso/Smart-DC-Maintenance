package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class BandHeartbeat {
	private String deviceId;
	//private Timestamp timestamp;
	private int heartFrequency;
	private int batteryLevel;
	
	
	public BandHeartbeat(String deviceId, /*Timestamp timestamp,*/ int heartFrequency, int batteryLevel) {
		this.deviceId = deviceId;
		//this.timestamp = timestamp;
		this.heartFrequency = heartFrequency;
		this.batteryLevel = batteryLevel;
	}
	
	public String getDeviceId() { return deviceId; }
	//public Timestamp getTimestamp() { return timestamp; }
	public int getHeartFrequency() { return heartFrequency; }
	public int getBatteryLevel() { return batteryLevel; }
}
