package it.unipi.dii.iot.mqtt;

import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.Band;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.paho.client.mqttv3.*;

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
        
        String[] fields = new String(mqttMessage.getPayload()).split("$");

        switch(action) {
            case "status": {
                Boolean active = fields[0].equals("active");
                Band band = new Band(id, active, 100, false);
                if (mySQLManager.updateBand(band) == 1) {
                    mySQLManager.insertBand(band);
                }
                break;
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }
}
