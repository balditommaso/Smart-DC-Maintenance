package it.unipi.dii.iot.model;

public class Vehicle {
	private String id;
	private String type;
	private String baseStation;
	private Boolean locked;
	
	public Vehicle(String id, String type, String baseStation, Boolean locked) {
		this.id = id;
		this.type = type;
		this.baseStation = baseStation;
		this.locked = locked;
	}
	
	public String getId() { return id; }
	public String getType() { return type; }
	public String getBaseStation() { return baseStation; }
	public Boolean getLocked() { return locked; }
}
