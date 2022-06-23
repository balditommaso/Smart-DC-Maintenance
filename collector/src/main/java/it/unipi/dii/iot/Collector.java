package it.unipi.dii.iot;

import it.unipi.dii.iot.model.Vehicle;
import it.unipi.dii.iot.mqtt.MQTTDriver;
import it.unipi.dii.iot.persistence.MySQLDriver;


public class Collector
{
    public static void main( String[] args )
    {
        //MQTTDriver mqttDriver = new MQTTDriver();
        
        MySQLDriver mysqlDriver = MySQLDriver.getInstance();
        
        Vehicle v = new Vehicle("ciao", "bike", "127.1.1.1", true);

        mysqlDriver.insertVehicle(v);
    }
}
