package it.unipi.dii.iot;

import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.mqtt.MqttCollector;
import it.unipi.dii.iot.persistence.MySQLDriver;


public class Collector
{
    public static void main( String[] args )
    {
        MqttCollector mqttDriver = new MqttCollector();
        mqttDriver.start();
        
        //mqttDriver.publish("000300030003", "band", "alert", "prova");
    }
}
