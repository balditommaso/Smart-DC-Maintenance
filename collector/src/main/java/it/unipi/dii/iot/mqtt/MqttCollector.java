package it.unipi.dii.iot.mqtt;

import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.model.BandSample;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.awt.List;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MqttCollector implements MqttCallback {
    private final Logger logger;
    
    private MySQLManager mySQLManager;
    private SampleCollector sampleCollector;
    private MqttClient mqttClient;
    private final String clientId;
    private final String brokerURI;
    
    // Thresholds
    private final int maxSamplesCollected;
   // private final int oxygenSaturationThreshold;


    public MqttCollector () {
    	logger = Logger.getLogger("it.unipi.SDCM");
    	
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        clientId = "Collector1";
        brokerURI = "tcp://" + configParameters.getBrokerIp() + ":" + configParameters.getBrokerPort();
        maxSamplesCollected = 20; 	// configParameters.getMaxSamplesCollected()
       // oxygenSaturationThreshold = configParameters.get
    }
    
    public void stop() {
        try {
            logger.log(Level.INFO, "Stopping the MQTT collector.");
            this.mqttClient.close(true);
            
        } catch (MqttException mqttException) {
            logger.log(Level.INFO, "An error occurred while stopping the MQTT collector.");
        }
    }
    
    public void start() {
    	logger.log(Level.INFO, "Starting the MQTT driver.");
    	
        try {
        	logger.log(Level.INFO, String.format("Connecting to the broker %s.", brokerURI));
            mqttClient = new MqttClient(brokerURI, clientId);
            mqttClient.connect();
            mqttClient.setCallback(this);
            logger.log(Level.INFO, "Connected to the broker.");
            
            mqttClient.subscribe(Topic.ALL_COMMANDS_TOWARDS_COLLECTOR);
            mqttClient.subscribe(Topic.ALL_BANDS_STATUS);
            mqttClient.subscribe(Topic.ALL_BANDS_SAMPLES);
            logger.log(Level.INFO, String.format("Subscribed correctly to the topics %s, %s, %s", 
            		Topic.ALL_COMMANDS_TOWARDS_COLLECTOR,
            		Topic.ALL_BANDS_STATUS,
            		Topic.ALL_BANDS_SAMPLES));
            
        	logger.log(Level.INFO, String.format("Connecting to the database %s.", brokerURI));
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
            logger.log(Level.INFO, String.format("Connected to the database."));       
            
        } catch (SQLException e) {
            logger.log(Level.INFO, "Failed to connect to the database.");
            e.printStackTrace();
            stop();
            
        } catch (MqttException me) {
        	logger.log(Level.INFO, "Failed to connect to the broker.");
        	me.printStackTrace();
            stop();
        }
        
        sampleCollector = new SampleCollector(maxSamplesCollected);
    }

    public void activateAlarm(String bandId, int alarmCode) {
    	String topic = String.format(Topic.TURN_ON_ALARM, bandId);
    	String message = "++++";
    	
    	if (alarmCode == 1)
    		message = "Oxygen Saturation too low";
    	
        try {
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        logger.log(Level.INFO, "Lost the connection with the broker.");
        stop();
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {    	 
    	logger.log(Level.INFO, String.format("New message: [%s], %s.", topic, new String(mqttMessage.getPayload())));
    	
        Gson parser = new Gson();
        String payload = new String(mqttMessage.getPayload());  
    	
    	if (Topic.isBandRegistration(topic)) {
            logger.log(Level.INFO, "Registering the sensor in the Database.");
            
    		BandDevice bandDevice = parser.fromJson(payload, BandDevice.class);
    		mySQLManager.insertBand(bandDevice);
    	}   
    	
    	if (Topic.isBandStatus(topic)) {
            logger.log(Level.INFO, "Updating the sensor status in the Database.");
            
            BandDevice bandDevice = parser.fromJson(payload, BandDevice.class);
            bandDevice.setBandId(Topic.getBandId(topic));
            System.out.println(bandDevice.toString());
            mySQLManager.updateBand(bandDevice);
    	}
    	
    	if (Topic.isBandSample(topic)) {
            logger.log(Level.INFO, "Registering the sample in the Database.");

    		BandSample bandSample = parser.fromJson(payload, BandSample.class);
        	bandSample.setBandId(Topic.getBandId(topic));
        	bandSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        	
        	//if (bandSample.isValid())
        	mySQLManager.insertBandSample(bandSample);
        	
            logger.log(Level.INFO, "Adding the sample to the local cache.");
        	sampleCollector.addBandSample(bandSample);
        	
            logger.log(Level.INFO, "Checking if alarm has to be activated.");
        	int ret = checkForAlarms(bandSample.getBandId());
        	
        	if (ret != -1) {
                logger.log(Level.INFO, "Activating the alarm on the band.");
                activateAlarm(bandSample.getBandId(), ret); 
        	}
    	}
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }
    
    private Integer checkForAlarms(String bandId) {
    	double[] avgs = sampleCollector.calculateWeightedAverages(bandId);
    	
    	if (avgs[0] < /*OXYGEN_SATURATION_THRESHOLD*/ 94)
    		return 0;
    	
    	if (avgs[1] < 80 || avgs[1] > 140)
    		return 1;
    	
    	if (avgs[2] < 36 || avgs[2] > 37)
    		return 2;
    	
    	if (avgs[3] < 15 || avgs[3] > 20)
    		return 3;
    	
    	if (avgs[4] < 60 || avgs[4] > 100)
    		return 4;
    	
    	return -1;
    }
}
