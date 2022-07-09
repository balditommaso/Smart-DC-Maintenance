package it.unipi.dii.iot.model;

import java.util.ArrayList;
import java.util.List;

public class BandDevice {
	private String bandId;
	private Boolean active;
	private Boolean alertOn;
	
	public BandDevice(String bandId, Boolean active, Boolean alertOn) {
		this.bandId = bandId;
		this.active = active;
		this.alertOn = alertOn;
	}
	
	public String getId() { return bandId; }
	public Boolean getActive() { return active; }
	public Boolean getAlertOn() { return alertOn; }
	public void setBandId(String bandId) { this.bandId = bandId; }
	public void setActive(Boolean active) { this.active = active; }
	public void setAlertOn(Boolean alertOn) { this.alertOn = alertOn; }
	
	public String toString() {
		String str = "bandId: " + bandId +", active: " + active +
				" , alertOn: " + alertOn;
		return str;
	}
}
