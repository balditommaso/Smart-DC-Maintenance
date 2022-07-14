package it.unipi.dii.iot.coap;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.RackSensor;
import it.unipi.dii.iot.model.TemperatureSample;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.io.StringReader;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CoAPTemperatureObserver {

    private MySQLManager mySQLManager;
    private final CoapClient client;
    private final int upperBound;
    private final int lowerBound;
    private final RackSensor rack;
    private final CoapObserveRelation relation;

    public CoAPTemperatureObserver(RackSensor rack) {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        upperBound = configParameters.getTemperatureUpperBound();
        lowerBound = configParameters.getTemperatureLowerBound();

        client = new CoapClient("coap://[" + rack.getRackSensorId() + "]:" + configParameters.getCoapPort()
                + "/" + configParameters.getTemperatureResource());

        try {
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.rack = rack;
        System.out.printf("INFO: Start observing temperature of %s\n", rack.getRackSensorId());
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

                        TemperatureSample sample = parser.fromJson(reader, TemperatureSample.class);
                        sample.setId(rack.getRackSensorId());
                        sample.setTimestamp(new Timestamp(System.currentTimeMillis()));

                        // ADD to DB
                        mySQLManager.insertTemperatureSample(sample);
                        // verify threshold
                        if ((sample.getValue() <= lowerBound || sample.getValue() >= upperBound)
                                && !rack.getAlarm()) {
                            // set alarm
                            rack.setAlarm(true);
                            mySQLManager.updateRackSensor(rack);
                            CoAPHandleResource.setResourceAlarm(client, true);
                        } else if ((sample.getValue() > lowerBound && sample.getValue() < upperBound)
                                && rack.getAlarm()) {
                            // reset alarm
                            rack.setAlarm(false);
                            mySQLManager.updateRackSensor(rack);
                            CoAPHandleResource.setResourceAlarm(client, false);
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

    public void stopObservingResource() {
        relation.proactiveCancel();
        CoAPHandleResource.removeResource(rack);
    }
}


