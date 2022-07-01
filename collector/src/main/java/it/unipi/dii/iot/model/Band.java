package it.unipi.dii.iot.model;

public class Band {
	private String id;
	private Boolean active;
	private int battery;
	private Boolean alertOn;
	
	public Band(String id, Boolean active, int battery, Boolean alertOn) {
		this.id = id;
		this.active = active;
		this.battery = battery;
		this.alertOn = alertOn;
	}
	
	public String getId() { return id; }
	public Boolean getActive() { return active; }
	public int getBattery() { return battery; }
	public Boolean getAlertOn() { return alertOn; }
	
}
