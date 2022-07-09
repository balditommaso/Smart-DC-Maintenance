package it.unipi.dii.iot.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unipi.dii.iot.model.BandSample;

public class SampleCollector {
	
	private Map<String, List<BandSample>> lastSamples;
	private final int maxSamples;
	
	public SampleCollector(int maxSamples) {
		lastSamples = new HashMap<>();
		this.maxSamples = maxSamples;
    }
	
	public void addBandSample (BandSample bandSample) {
		// Init a new map element if the device is not present
		if (!lastSamples.containsKey(bandSample.getBandId())) {
			lastSamples.put(bandSample.getBandId(), new ArrayList<>());
		}
				
		// Remove last sample
		if (lastSamples.get(bandSample.getBandId()).size() > 20) {
			lastSamples.get(bandSample.getBandId()).remove(lastSamples.get(bandSample.getBandId()).size()-1);
		}
		
		// Add new sample
		lastSamples.get(bandSample.getBandId()).add(bandSample);
	}
	
	public double[] calculateWeightedAverages(String bandId) throws ArithmeticException {
		// 0 oxygenSaturation, 1 bloodPressure, 2 temperature, 3 respiration, 4 heartRate
		List<BandSample> samples = lastSamples.get(bandId);
		double[] results = new double[5];
		if (samples == null)
			return results;

        for (int i=0; i<samples.size(); i++) {
        	results[0] = results[0] + (i+1)*samples.get(i).getOxygenSaturation();
        	results[1] = results[1] + (i+1)*samples.get(i).getBloodPressure();
        	results[2] = results[2] + (i+1)*samples.get(i).getTemperature();
        	results[3] = results[3] + (i+1)*samples.get(i).getRespiration();
        	results[4] = results[4] + (i+1)*samples.get(i).getHeartRate();
        }
        
        double denom = (samples.size() * (samples.size()-1))/2;
        if (denom == 0) denom = 1;
        
        for (int i=0; i<5; i++)
        	results[i] = results[i]/denom;

        return results;
    }
	
}
