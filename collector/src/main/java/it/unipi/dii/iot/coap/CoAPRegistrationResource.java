package it.unipi.dii.iot.coap;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Arrays;

public class CoAPRegistrationResource extends CoapResource {

    public CoAPRegistrationResource(String name) {
        super(name);
    }

    public CoAPRegistrationResource(String name, boolean visible) {
        super(name, visible);
    }

    public void handlePOST(CoapExchange exchange) {
        Response response = new Response(CoAP.ResponseCode.CONTINUE);
        System.out.println("Coap registration request: " + Arrays.toString(exchange.getRequestPayload()));
        // accept only JSON encoding
        if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON) {
            response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            System.out.println("JSON encoding accepted");
        }
        exchange.respond(response);
    }
}
