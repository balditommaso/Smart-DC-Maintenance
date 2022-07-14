package it.unipi.dii.iot.model;

import java.sql.Timestamp;

public class BandSample {
	private String bandId;
	private int batteryLevel;
	private int oxygenSaturation;
	private int bloodPressure;
	private double temperature;
	private int respiration;
	private int heartRate;
	
	public BandSample(String bandId, int batteryLevel, int oxygenSaturation,
			int bloodPressure, int temperature, int respiration, int heartRate) {
		this.bandId = bandId;
		this.batteryLevel = batteryLevel;
		this.oxygenSaturation = oxygenSaturation;
		this.bloodPressure = bloodPressure;
		this.temperature = temperature/10.0;
		this.respiration = respiration;
		this.heartRate = heartRate;
	}
	
	public String getBandId() { return bandId; }
	public int getBatteryLevel() { return batteryLevel; }
	public int getOxygenSaturation() { return oxygenSaturation; }
	public int getBloodPressure() { return bloodPressure; }
	public double getTemperature() { return temperature; }
	public int getRespiration() { return respiration; }
	public int getHeartRate() { return heartRate; }
	public void setBandId(String bandId) { this.bandId = bandId; }
	public void setTemperature(double temperature) { this.temperature = temperature; }
	
	public String toString() {
		String str = "bandId: " + bandId +
				" , batteryLevel: " + batteryLevel + ", oxygenSaturation: " + oxygenSaturation + 
				" , bloodPressure: " + bloodPressure + ", temperature: " + temperature + 
				" , respiration: " + respiration + ", heartRate: " + heartRate;
		return str;
	}
}
