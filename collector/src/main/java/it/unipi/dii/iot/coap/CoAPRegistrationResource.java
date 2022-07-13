package it.unipi.dii.iot.coap;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.model.RackSensor;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class CoAPRegistrationResource extends CoapResource {

     private MySQLManager mySQLManager;
     private HashMap<String, CoAPTemperatureObserver> activeResources;

    public CoAPRegistrationResource(String name) {
        super(name);
        activeResources = new HashMap<>();
        try {
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CoAPRegistrationResource(String name, boolean visible) {
        super(name, visible);
    }

    public void handlePOST(CoapExchange exchange) {
        Response response = new Response(CoAP.ResponseCode.CONTINUE);
        System.out.println("Coap registration request: " + new String(exchange.getRequestPayload()));

        // aggiungi al DB
        Gson parser = new Gson();
        String payload = new String(exchange.getRequestPayload());
        JsonReader reader = new JsonReader(new StringReader(payload));
        reader.setLenient(true);
        RackSensor rackSensor = null;
        try {
            rackSensor = parser.fromJson(reader, RackSensor.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(rackSensor.toString());
        mySQLManager.insertRackSensor(rackSensor);

        if (!activeResources.containsKey(rackSensor.getRackSensorId())) {
            System.out.println("active observing");
            CoAPTemperatureObserver temperatureObserver = new CoAPTemperatureObserver(rackSensor);
            activeResources.put(rackSensor.getRackSensorId(), temperatureObserver);
        }

        // rispondi
        response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
        response.setPayload("{ \"registration\": 1}");
        exchange.respond(response);
    }
}
