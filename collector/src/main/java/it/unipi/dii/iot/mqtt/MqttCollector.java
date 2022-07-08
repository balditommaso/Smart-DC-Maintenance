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
    
    private final int maxSamplesCollected;


    public MqttCollector () {
    	logger = Logger.getLogger("it.unipi.SDCM");
    	
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        clientId = "Collector1";
        brokerURI = "tcp://" + configParameters.getBrokerIp() + ":" + configParameters.getBrokerPort();
        maxSamplesCollected = 20; 	// configParameters.getMaxSamplesCollected()
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
            mqttClient.subscribe(Topic.ALL_BANDS);
            logger.log(Level.INFO, String.format("Subscribed correctly to the topics %s, %s", 
            		Topic.ALL_COMMANDS_TOWARDS_COLLECTOR,
            		Topic.ALL_BANDS));
            
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

    public void publish(String id, String topic, String action, String message) {
        try {
            mqttClient.publish(topic + "/" + id + "/" + action, new MqttMessage(message.getBytes()));
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
    		//BandDevice bandDevice = parser.fromJson(payload, BandDevice.class);
    		//mySQLManager.insertBand(bandDevice);
    	}   
    	
    	if (Topic.isBandStatus(topic)) {
            BandDevice bandDevice = parser.fromJson(payload, BandDevice.class);
            if (mySQLManager.updateBand(bandDevice) == 1) {
                mySQLManager.insertBand(bandDevice);
            }
    	}
    	
    	if (Topic.isBandSample(topic)) {
    		BandSample bandSample = parser.fromJson(payload, BandSample.class);
        	bandSample.setBandId(Topic.getBandId(topic));
        	bandSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        	mySQLManager.insertBandSample(bandSample);
        	
        	sampleCollector.addBandSample(bandSample);
        	
        	int ret = checkForAlarms(bandSample.getBandId());
        	
        	if (ret != -1)
        		publish(bandSample.getBandId(), "SDCM", "alarm", String.valueOf(ret));            	
    	}
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }
    
    private Integer checkForAlarms(String bandId) {
    	ArrayList<Double> avgs = (ArrayList<Double>) sampleCollector.calculateWeightedAverages(bandId);
    	
    	if (avgs.get(0) < /*OXYGEN_SATURATION_THRESHOLD*/ 94)
    		return 0;
    	
    	if (avgs.get(1) < 80 || avgs.get(1) > 140)
    		return 1;
    	
    	if (avgs.get(2) < 36 || avgs.get(2) > 37)
    		return 2;
    	
    	if (avgs.get(3) < 15|| avgs.get(3) > 20)
    		return 3;
    	
    	if (avgs.get(4) < 60|| avgs.get(4) > 100)
    		return 4;
    	
    	return -1;
    }
}
