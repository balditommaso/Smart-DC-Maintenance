package it.unipi.dii.iot.config;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigParameters {   
    private String databaseIp;
    private int databasePort;
    private String databaseUsername;
    private String databasePassword;
    private String databaseName;

    private String brokerIp;
    private int brokerPort;
    private int maxSamplesCache;
    
    private int oxygenSaturationThreshold;
    private int bloodPressureLowerThreshold;
    private int bloodPressureHigherThreshold;
    private double temperatureLowerThreshold;
    private double temperatureHigherThreshold;
    private int respirationLowerThreshold;
    private int respirationHigherThreshold;
    private int heartRateLowerThreshold;
    private int heartRateHigherThreshold;

    private int coapPort;
    private String registrationResource;
    private String temperatureResource;
    private String humidityResource;
    private String oxygenResource;

    private int temperatureLowerBound;
    private int temperatureUpperBound;
    private int humidityLowerBound;
    private int humidityUpperBound;
    private float oxygenLowerBound;
    private float oxygenUpperBound;


    public ConfigParameters(String configFilePath) {
    	try{
            FileInputStream fis = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(fis);
            
            databaseIp = prop.getProperty("databaseIp");
            databasePort = Integer.parseInt(prop.getProperty("databasePort"));
            databaseUsername = prop.getProperty("databaseUsername");
            databasePassword = prop.getProperty("databasePassword");
            databaseName = prop.getProperty("databaseName");

            brokerIp = prop.getProperty("brokerIp");
            brokerPort = Integer.parseInt(prop.getProperty("brokerPort"));
            maxSamplesCache = Integer.parseInt(prop.getProperty("maxSamplesCache"));

            coapPort = Integer.parseInt(prop.getProperty("coapPort"));
            registrationResource = prop.getProperty("registrationResource");
            temperatureResource = prop.getProperty("temperatureResource");
            humidityResource = prop.getProperty("humidityResource");
            oxygenResource = prop.getProperty("oxygenResource");

            temperatureLowerBound = Integer.parseInt(prop.getProperty("temperatureLowerBound"));
            temperatureUpperBound = Integer.parseInt(prop.getProperty("temperatureUpperBound"));
            humidityLowerBound = Integer.parseInt(prop.getProperty("humidityLowerBound"));
            humidityUpperBound = Integer.parseInt(prop.getProperty("humidityUpperBound"));
            oxygenLowerBound = Float.parseFloat(prop.getProperty("oxygenLowerBound"));
            oxygenUpperBound = Float.parseFloat(prop.getProperty("oxygenUpperBound"));

            oxygenSaturationThreshold = Integer.parseInt(prop.getProperty("oxygenSaturationThreshold"));
            bloodPressureLowerThreshold = Integer.parseInt(prop.getProperty("bloodPressureLowerThreshold")); 
            bloodPressureHigherThreshold = Integer.parseInt(prop.getProperty("bloodPressureHigherThreshold")); 
            temperatureLowerThreshold = Double.parseDouble(prop.getProperty("temperatureLowerThreshold")); 
            temperatureHigherThreshold = Double.parseDouble(prop.getProperty("temperatureHigherThreshold")); 
            respirationLowerThreshold = Integer.parseInt(prop.getProperty("respirationLowerThreshold")); 
            respirationHigherThreshold = Integer.parseInt(prop.getProperty("respirationHigherThreshold")); 
            heartRateLowerThreshold = Integer.parseInt(prop.getProperty("heartRateLowerThreshold")); 
            heartRateHigherThreshold = Integer.parseInt(prop.getProperty("heartRateHigherThreshold")); 
            
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        validate();
    }
    
    private void validate() {
    	
    }
    
    public String getDatabaseIp() { return databaseIp; }
    public Integer getDatabasePort() { return databasePort; }
    public String getDatabaseUsername() { return databaseUsername; }
    public String getDatabasePassword() { return databasePassword; }
    public String getDatabaseName() { return databaseName; }

    public String getBrokerIp() { return brokerIp; }
    public int getBrokerPort() { return brokerPort; }
    public int getMaxSamplesCache() { return maxSamplesCache; }

    public int getCoapPort() {
        return coapPort;
    }

    public String getRegistrationResource() {
        return registrationResource;
    }

    public String getTemperatureResource() {
        return temperatureResource;
    }

    public String getHumidityResource() {
        return humidityResource;
    }

    public String getOxygenResource() {
        return oxygenResource;
    }

    public int getTemperatureLowerBound() {
        return temperatureLowerBound;
    }

    public int getTemperatureUpperBound() {
        return temperatureUpperBound;
    }

    public int getHumidityLowerBound() {
        return humidityLowerBound;
    }

    public int getHumidityUpperBound() {
        return humidityUpperBound;
    }

    public float getOxygenLowerBound() {
        return oxygenLowerBound;
    }

    public float getOxygenUpperBound() {
        return oxygenUpperBound;
    }

    public int getOxygenSaturationThreshold() { return oxygenSaturationThreshold; }
    public int getBloodPressureLowerThreshold() { return bloodPressureLowerThreshold; }
    public int getBloodPressureHigherThreshold() { return bloodPressureHigherThreshold; }
    public double getTemperatureLowerThreshold() { return temperatureLowerThreshold; }
    public double getTemperatureHigherThreshold() { return temperatureHigherThreshold; }
    public int getRespirationLowerThreshold() { return respirationLowerThreshold; }
    public int getRespirationHigherThreshold() { return respirationHigherThreshold; }
    public int getHeartRateLowerThreshold() { return heartRateLowerThreshold; }
    public int getHeartRateHigherThreshold() { return heartRateHigherThreshold; }
}
