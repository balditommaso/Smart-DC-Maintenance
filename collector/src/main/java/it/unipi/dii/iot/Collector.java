package it.unipi.dii.iot;

import it.unipi.dii.iot.coap.CoAPServer;
import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.mqtt.MqttCollector;
import it.unipi.dii.iot.persistence.MySQLDriver;



public class Collector
{
    public static void main( String[] args )
    {
        // start MQTT collector
        MqttCollector mqttDriver = new MqttCollector();
        mqttDriver.start();

        // start CoAP Server to register active devices
        CoAPServer coapServer = new CoAPServer();
        //mqttDriver.publish("000300030003", "band", "alert", "prova");
    }
}
