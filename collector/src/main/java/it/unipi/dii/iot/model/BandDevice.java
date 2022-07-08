package it.unipi.dii.iot.model;

import java.util.ArrayList;
import java.util.List;

public class BandDevice {
	private String id;
	private Boolean active;
	private Boolean alertOn;
	
	public BandDevice(String id, Boolean active, Boolean alertOn) {
		this.id = id;
		this.active = active;
		this.alertOn = alertOn;
	}
	
	public String getId() { return id; }
	public Boolean getActive() { return active; }
	public Boolean getAlertOn() { return alertOn; }
	
}
