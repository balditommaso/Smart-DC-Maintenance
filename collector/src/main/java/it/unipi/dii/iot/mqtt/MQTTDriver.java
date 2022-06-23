package it.unipi.dii.iot.mqtt;

import org.eclipse.paho.client.mqttv3.*;

import it.unipi.dii.iot.model.Vehicle;
import it.unipi.dii.iot.persistence.MySQLDriver;

import java.nio.charset.StandardCharsets;

public class MQTTDriver implements MqttCallback {

    // TODO: set in config file
    private MqttClient mqttClient;
    private String broker = "tcp://172.16.4.159:1883";
    private String clientId = "Collector";
    private int maxAttempt = 10;
    private int secondsToWait = 1000;


    public MQTTDriver () {
        try {
            mqttClient = new MqttClient(broker, clientId);
            mqttClient.connect();
            mqttClient.setCallback(this);
            mqttClient.subscribe("charger/#");
            System.out.println("Subscribe correctly.");
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    public void publish(String id, String topic, String message) {
        try {
            mqttClient.publish(topic + "/" + id, new MqttMessage(message.getBytes(StandardCharsets.UTF_8)));
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
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        System.out.println(String.format("[%s] %s", topic, new String(mqttMessage.getPayload())));
        
        String[] tokens = topic.split("/");
        String type = tokens[0];
        String id = tokens[1];
        String action = tokens[2];
        
        String[] fields = new String(mqttMessage.getPayload()).split("$");
        String baseStation = fields[0];       
        Boolean locked = Boolean.parseBoolean(fields[1]);
        
        // TODO: compute traffic, store data in DB
        switch(action) {
        	case "register":
        		Vehicle vehicle = new Vehicle(id, type, "", locked);
        		MySQLDriver.getInstance().insertVehicle(vehicle);
        	break;
        
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }
}
