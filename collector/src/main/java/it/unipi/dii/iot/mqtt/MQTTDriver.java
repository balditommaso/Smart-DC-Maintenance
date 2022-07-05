package it.unipi.dii.iot.mqtt;

import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.model.BandSample;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MQTTDriver implements MqttCallback {
    private final Logger logger;
    private MySQLManager mySQLManager;
    private MqttClient mqttClient;
    private final String clientId;
    private final int maxAttempt;
    private final int secondsToWait;
    private final String brokerURI;


    public MQTTDriver () {
    	logger = Logger.getLogger("it.unipi.SDCM");
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        clientId = "Collector1";
        maxAttempt = configParameters.getMaxAttempt();
        secondsToWait = configParameters.getSecondsToWait();
        brokerURI = "tcp://" + configParameters.getBrokerIp() + ":" + configParameters.getBrokerPort();
    }
    
    public void start() {
    	logger.log(Level.INFO, "Starting the MQTT driver.");
    	
        try {
        	logger.log(Level.INFO, String.format("Attempting to connect to the broker %s.", brokerURI));
            mqttClient = new MqttClient(brokerURI, clientId);
            mqttClient.connect();
            mqttClient.setCallback(this);
            mqttClient.subscribe("SDCM/band/#");
        } catch (MqttException me) {
            me.printStackTrace();
        } 
        logger.log(Level.INFO, String.format("Subscribed correctly."));
        
        try {
        	logger.log(Level.INFO, String.format("Attempting to connect to the database %s.", brokerURI));
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }    	
        logger.log(Level.INFO, String.format("Connected."));
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
        int attempt = 0;
        while(attempt < maxAttempt) {
            attempt++;
            try {
                Thread.sleep(secondsToWait);
                mqttClient.connect();
            } catch (InterruptedException | MqttException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
            if (mqttClient.isConnected())
                break;
        }
        System.exit(1);
    }
    // TODO: compute traffic, store data in DB
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {    	 
    	logger.log(Level.INFO, String.format("New message: [%s], %s.", topic, new String(mqttMessage.getPayload())));
    	
        String[] tokens = topic.split("/");
        String bandId = tokens[2];
        String action = tokens[3];
        
        Gson parser = new Gson();
        String payload = new String(mqttMessage.getPayload());     

        switch(action) {		// Topic.java
            case "status": {
                BandDevice bandDevice = parser.fromJson(payload, BandDevice.class);
                if (mySQLManager.updateBand(bandDevice) == 1) {
                    mySQLManager.insertBand(bandDevice);
                }
                break;
            }
            case "sample": {
            	BandSample bandSample = parser.fromJson(payload, BandSample.class);
            	bandSample.setBandId(bandId);
            	bandSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
            	mySQLManager.insertBandSample(bandSample);
            	break;
            }
            default: {
            	logger.log(Level.INFO, "Discarding the message: unknown topic.");
            }
            	
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }
}
