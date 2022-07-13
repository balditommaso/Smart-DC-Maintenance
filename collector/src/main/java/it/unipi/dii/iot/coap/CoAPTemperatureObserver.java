package it.unipi.dii.iot.coap;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.RackSample;
import it.unipi.dii.iot.model.RackSensor;
import it.unipi.dii.iot.persistence.MySQLDriver;
import it.unipi.dii.iot.persistence.MySQLManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.io.StringReader;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CoAPTemperatureObserver {

    private MySQLManager mySQLManager;
    private CoapClient client;
    private int upperBound;
    private int lowerBound;
    private RackSensor rack;

    public CoAPTemperatureObserver(RackSensor rack) {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        upperBound = configParameters.getTemperatureUpperBound();
        lowerBound = configParameters.getTemperatureLowerBound();

        client = new CoapClient("coap://[" + rack.getRackSensorId() + "]/" + configParameters.getTemperatureResource());

        try {
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.rack = rack;
        System.out.printf("INFO: Start observing temperature of %s%n", rack.getRackSensorId());
        CoapObserveRelation relation = client.observe(
                new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse coapResponse) {
                        // read response JSON
                        String content = coapResponse.getResponseText();
                        System.out.printf("INFO: received from %s: %s%n", rack.getRackSensorId(), content);

                        Gson parser = new Gson();
                        JsonReader reader = new JsonReader(new StringReader(content));
                        reader.setLenient(true);

                        RackSample rackSample = parser.fromJson(reader, RackSample.class);
                        rackSample.setRackSensorId(rack.getRackSensorId());
                        rackSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
                        rackSample.setMeasure(configParameters.getTemperatureResource());
                        // ADD to DB
                        mySQLManager.insertRackSensorSample(rackSample);
                        // verify threshold
                        if ((rackSample.getValue() <= lowerBound || rackSample.getValue() >= upperBound)
                            && !rack.getAlarm()) {
                            // set alarm
                            rack.setAlarm(true);
                            mySQLManager.updateRackSensor(rack);

                            try {
                                client.put("{ \"registration\": 1}", MediaTypeRegistry.APPLICATION_JSON);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if ((rackSample.getValue() >= lowerBound && rackSample.getValue() <= upperBound)
                                    && rack.getAlarm()) {
                            // reset alarm
                            rack.setAlarm(false);
                            mySQLManager.updateRackSensor(rack);
                            try {
                                client.put("{ \"registration\": 0}", MediaTypeRegistry.APPLICATION_JSON);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError() {
                        System.err.printf("ERROR: fail to observe %s%n", rack.getRackSensorId());
                    }
                }
        );
        //relation.proactiveCancel();
    }
}
