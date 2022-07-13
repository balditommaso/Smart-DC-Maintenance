package it.unipi.dii.iot.coap;

import com.google.gson.Gson;
import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.model.RackSensor;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.sql.SQLException;
import java.util.Arrays;

public class CoAPRegistrationResource extends CoapResource {

     private MySQLManager mySQLManager;

    public CoAPRegistrationResource(String name) {
        super(name);
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
        RackSensor rackSensor = parser.fromJson(payload, RackSensor.class);
        mySQLManager.insertSensor(rackSensor);

        // osserva le risorse TODO


        // rispondi
        response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
        response.setPayload("{ \"registration\": \"success\"}");
        exchange.respond(response);
    }
}
