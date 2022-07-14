package it.unipi.dii.iot.coap;

import it.unipi.dii.iot.config.ConfigParameters;
import org.eclipse.californium.core.CoapServer;
public class CoAPServer extends CoapServer {

    public CoAPServer() {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        this.add(new CoAPHandleResource(configParameters.getRegistrationResource()));
        System.out.println("Start the CoAP Server");
        this.start();
    }

    private void stopServer() {
        this.stop();
    }

}
