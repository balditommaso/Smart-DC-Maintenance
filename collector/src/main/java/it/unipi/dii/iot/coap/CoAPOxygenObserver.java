package it.unipi.dii.iot.coap;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.OxygenSample;
import it.unipi.dii.iot.model.RackSensor;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import java.io.StringReader;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CoAPOxygenObserver {
    private MySQLManager mySQLManager;
    private final CoapClient client;
    private final float upperBound;
    private final float lowerBound;
    private final RackSensor rack;
    private final CoapObserveRelation relation;

    public CoAPOxygenObserver(RackSensor rack) {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        upperBound = configParameters.getOxygenUpperBound();
        lowerBound = configParameters.getOxygenLowerBound();

        client = new CoapClient("coap://[" + rack.getRackSensorId() + "]:" + configParameters.getCoapPort()
                + "/" + configParameters.getOxygenResource());

        try {
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.rack = rack;
        System.out.printf("INFO: Start observing oxygen of %s\n", rack.getRackSensorId());
        relation = client.observe(
                new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse coapResponse) {
                        // read response JSON
                        String content = new String(coapResponse.getPayload());
                        System.out.printf("INFO: received from %s: %s\n", rack.getRackSensorId(), content);

                        Gson parser = new Gson();
                        JsonReader reader = new JsonReader(new StringReader(content));
                        reader.setLenient(true);

                        OxygenSample sample = parser.fromJson(reader, OxygenSample.class);
                        sample.setId(rack.getRackSensorId());
                        sample.setTimestamp(new Timestamp(System.currentTimeMillis()));

                        // ADD to DB
                        mySQLManager.insertOxygenSample(sample);
                        // verify threshold
                        if ((sample.getValue() <= lowerBound || sample.getValue() >= upperBound)
                                && !rack.getAlarm()) {
                            // set alarm
                            System.out.printf("INFO: activating alarm to %s\n", rack.getRackSensorId());
                            rack.setAlarm(true);
                            mySQLManager.updateRackSensor(rack);
                            //TODO
                            setAlarm(true);
                        } else if ((sample.getValue() > lowerBound && sample.getValue() < upperBound)
                                && rack.getAlarm()) {
                            // reset alarm
                            System.out.printf("INFO: deactivating alarm to %s\n", rack.getRackSensorId());
                            rack.setAlarm(false);
                            mySQLManager.updateRackSensor(rack);
                            setAlarm(false);
                        }
                    }

                    @Override
                    public void onError() {
                        System.err.printf("ERROR: fail to observe %s\n", rack.getRackSensorId());
                        stopObservingResource();
                    }
                }
        );
    }

    public void setAlarm(boolean mode) {
        if (client == null)
            return;
        String message = "alarm=" + (mode ? 1 : 0);
        client.put(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse coapResponse) {
                if (coapResponse != null) {
                    if(!coapResponse.isSuccess())
                        System.out.printf("ERROR: Cannot send the PUT request to %s\n", rack.getRackSensorId());
                }
            }

            @Override
            public void onError() {
                System.out.printf("ERROR: Cannot contact %s\n", client.getURI());
            }
        }, message, MediaTypeRegistry.TEXT_PLAIN);
    }

    public void stopObservingResource() {
        relation.proactiveCancel();
        CoAPRegistrationResource.removeResource(rack);
    }
}

