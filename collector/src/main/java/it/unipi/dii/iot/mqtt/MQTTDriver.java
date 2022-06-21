package it.unipi.dii.iot.mqtt;

import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;

public class MQTTDriver implements MqttCallback {

    // TODO: set in config file
    private MqttClient mqttClient;
    private String broker = "tcp://127.0.0.1:1883";
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
        // TODO: compute traffic, store data in DB
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }
}
