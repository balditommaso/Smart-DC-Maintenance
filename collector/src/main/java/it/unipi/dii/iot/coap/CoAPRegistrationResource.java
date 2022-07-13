package it.unipi.dii.iot.coap;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
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
import java.util.ArrayList;
import java.util.HashMap;


public class CoAPRegistrationResource extends CoapResource {

     private MySQLManager mySQLManager;
     private static HashMap<String, ArrayList<Object>> activeResources;

    public CoAPRegistrationResource(String name) {
        super(name);
        activeResources = new HashMap<>();
        try {
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handlePOST(CoapExchange exchange) {
        Response response = new Response(CoAP.ResponseCode.CONTINUE);
        System.out.println("Coap registration request: " + new String(exchange.getRequestPayload()));

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

        if (rackSensor == null) {
            System.err.println("ERROR: Not valid object.");
            System.exit(1);
        }

        mySQLManager.insertRackSensor(rackSensor);
        System.out.println("Check if already active");
        if (!activeResources.containsKey(rackSensor.getRackSensorId())) {
            ArrayList<Object> resources = new ArrayList<>();
            resources.add(new CoAPTemperatureObserver(rackSensor));
            resources.add(new CoAPHumidityObserver(rackSensor));
            resources.add(new CoAPOxygenObserver(rackSensor));
            activeResources.put(rackSensor.getRackSensorId(), resources);
        }

        response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
        response.setPayload("{ \"registration\": 1}");
        exchange.respond(response);
    }

    public static void removeResource(RackSensor rack) {
        activeResources.remove(rack.getRackSensorId());
        System.out.printf("INFO: Removed the resource %s\n", rack.getRackSensorId());
    }
}
