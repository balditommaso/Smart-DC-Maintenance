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
	
	public List<Double> calculateWeightedAverages(String bandId) throws ArithmeticException {
		// 0 oxygenSaturation, 1 bloodPressure, 2 temperature, 3 respiration, 4 heartRate
		List<BandSample> samples = lastSamples.get(bandId);
		List<Double> results = new ArrayList<>(5);
		
        for (int i=0; i<samples.size(); i++) {
        	results.set(0, results.get(0) + (i+1)*samples.get(i).getOxygenSaturation());
        	results.set(1, results.get(1) + samples.get(i).getBloodPressure());
        	results.set(2, results.get(2) + samples.get(i).getTemperature());
        	results.set(3, results.get(3) + samples.get(i).getRespiration());
        	results.set(4, results.get(4) + samples.get(i).getHeartRate());
        }
        
        double denom = (samples.size() * (samples.size()-1))/2;
        
        for (int i=0; i<results.size(); i++)
        	results.set(i, results.get(i)/denom);

        return results;
    }
	
}
