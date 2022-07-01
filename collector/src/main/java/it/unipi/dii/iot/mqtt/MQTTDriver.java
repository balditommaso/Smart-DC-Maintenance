package it.unipi.dii.iot.mqtt;

import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.model.BandHeartbeat;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;

import java.sql.SQLException;

public class MQTTDriver implements MqttCallback {

    // TODO: clientId configurable
    private MySQLManager mySQLManager;
    private MqttClient mqttClient;
    private final String clientId;
    private final int maxAttempt;
    private final int secondsToWait;


    public MQTTDriver () {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        clientId = "Collector1";
        maxAttempt = configParameters.getMaxAttempt();
        secondsToWait = configParameters.getSecondsToWait();
        String broker = "tcp://" + configParameters.getBrokerIp() + ":" + configParameters.getBrokerPort();
        try {
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
            mqttClient = new MqttClient(broker, clientId);
            mqttClient.connect();
            mqttClient.setCallback(this);
            mqttClient.subscribe("band/#");
            System.out.println("Subscribed correctly.");
        } catch (MqttException me) {
            me.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        System.out.printf("[%s] %s%n", topic, new String(mqttMessage.getPayload()));
        
        String[] tokens = topic.split("/");
        String id = tokens[1];
        String action = tokens[2];
        
        Gson parser = new Gson();
        String payload = new String(mqttMessage.getPayload());        

        switch(action) {
            case "status": {
                BandDevice bandDevice = parser.fromJson(payload, BandDevice.class);
                if (mySQLManager.updateBand(bandDevice) == 1) {
                    mySQLManager.insertBand(bandDevice);
                }
                break;
            }
            case "heartbeat": {
            	BandHeartbeat bandHeartbeat = parser.fromJson(payload, BandHeartbeat.class);
            	mySQLManager.insertBandHeartbeat(bandHeartbeat);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }
}
