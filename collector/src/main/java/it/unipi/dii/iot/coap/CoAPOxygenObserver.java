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

import java.io.StringReader;
import java.sql.SQLException;

public class CoAPOxygenObserver {
    private MySQLManager mySQLManager;
    private final CoapClient sensor;
    private final CoapClient actuator;
    private final float upperBound;
    private final float lowerBound;
    private final RackSensor rack;
    private final CoapObserveRelation relation;

    public CoAPOxygenObserver(RackSensor rack) {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        upperBound = configParameters.getOxygenUpperBound();
        lowerBound = configParameters.getOxygenLowerBound();

        sensor = new CoapClient("coap://[" + rack.getRackSensorId() + "]:" + configParameters.getCoapPort()
                + "/" + configParameters.getOxygenResource());
        actuator = new CoapClient("coap://[" + rack.getRackSensorId() + "]:" + configParameters.getCoapPort()
                + "/" + configParameters.getActuatorResource());

        try {
            mySQLManager = new MySQLManager(MySQLDriver.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.rack = rack;
        System.out.printf("INFO: Start observing oxygen of %s\n", rack.getRackSensorId());
        relation = sensor.observe(
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

                        // ADD to DB
                        mySQLManager.insertOxygenSample(sample);
                        // verify threshold
                        if ((sample.getValue() <= lowerBound || sample.getValue() >= upperBound)
                                && !rack.getAlarm()) {
                            // set alarm
                            System.out.printf("INFO: activating alarm to %s\n", rack.getRackSensorId());
                            rack.setAlarm(true);
                            mySQLManager.updateRackSensor(rack);
                            CoAPResourceHandler.setResourceAlarm(sensor, true);
                            CoAPResourceHandler.setResourceAlarm(actuator, true);
                        } else if ((sample.getValue() > lowerBound && sample.getValue() < upperBound)
                                && rack.getAlarm()) {
                            // reset alarm
                            System.out.printf("INFO: deactivating alarm to %s\n", rack.getRackSensorId());
                            rack.setAlarm(false);
                            mySQLManager.updateRackSensor(rack);
                            CoAPResourceHandler.setResourceAlarm(sensor, false);
                            CoAPResourceHandler.setResourceAlarm(actuator, false);
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
        CoAPResourceHandler.removeResource(rack);
    }
}

