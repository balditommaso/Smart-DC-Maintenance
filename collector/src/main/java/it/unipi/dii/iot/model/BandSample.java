package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class BandSample {
	private String bandId;
	private Timestamp timestamp;
	private int batteryLevel;
	private int oxygenSaturation;
	private int bloodPressure;
	private double temperature;
	private int respiration;
	private int heartRate;
	
	public BandSample(String bandId, Timestamp timestamp, int batteryLevel, int oxygenSaturation,
			int bloodPressure, double temperature, int respiration, int heartRate) {
		this.bandId = bandId;
		this.timestamp = timestamp;
		this.batteryLevel = batteryLevel;
		this.oxygenSaturation = oxygenSaturation;
		this.bloodPressure = bloodPressure;
		this.temperature = temperature;
		this.respiration = respiration;
		this.heartRate = heartRate;
	}
	
	public String getBandId() { return bandId; }
	public Timestamp getTimestamp() { return timestamp; }
	public int getBatteryLevel() { return batteryLevel; }
	public int getOxygenSaturation() { return oxygenSaturation; }
	public int getBloodPressure() { return bloodPressure; }
	public double getTemperature() { return temperature; }
	public int getRespiration() { return respiration; }
	public int getHeartRate() { return heartRate; }
	public void setBandId(String bandId) { this.bandId = bandId; }
	public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
	
	public String toString() {
		String str = "bandId: " + bandId +", timestamp: " + timestamp +
				" , batteryLevel: " + batteryLevel + ", oxygenSaturation: " + oxygenSaturation + 
				" , bloodPressure: " + bloodPressure + ", temperature: " + temperature + 
				" , respiration: " + respiration + ", heartRate: " + heartRate;
		return str;
	}
}
