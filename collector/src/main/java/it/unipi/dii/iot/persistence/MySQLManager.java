package it.unipi.dii.iot.persistence;

import it.unipi.dii.iot.model.*;


import java.sql.*;

public class MySQLManager {

    private final Connection connection;

    public MySQLManager (Connection connection) {
        this.connection = connection;
    }

    public void insertBand (BandDevice bandDevice) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO band_device (idband, active, alertOn) VALUES (?, ?, ?)")
        ){
            statement.setString(1, bandDevice.getId());
            statement.setBoolean(2, bandDevice.getActive());
            statement.setBoolean(3, bandDevice.getAlertOn());
            statement.executeUpdate();
        }
        catch (final SQLIntegrityConstraintViolationException e) {
            System.out.printf("INFO: band device %s already registered in the database.%n", bandDevice.getId());
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBand (BandDevice bandDevice) {
        try (
                PreparedStatement statement = connection.prepareStatement("UPDATE band_device SET active = ?, alertOn = ? WHERE idband = ?")
        ){
            statement.setBoolean(1, bandDevice.getActive());
            statement.setBoolean(2, bandDevice.getAlertOn());
            statement.setString(3, bandDevice.getId());
            if (statement.executeUpdate() != 1) throw new Exception("ERROR: not valid id " + bandDevice.getId());
        }
        catch (final SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void insertBandSample (BandSample bandSample) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO band_samples "
                		+ "(bandid,batteryLevel,oxygenSaturation,bloodPressure,temperature,respiration,heartRate)"
                		+ " VALUES (?, ?, ?, ?, ?, ?, ?)")
        ){
            statement.setString(1, bandSample.getBandId());
            statement.setInt(2, bandSample.getBatteryLevel());
            statement.setInt(3, bandSample.getOxygenSaturation());
            statement.setInt(4, bandSample.getBloodPressure());
            statement.setDouble(5, bandSample.getTemperature());
            statement.setInt(6, bandSample.getRespiration());
            statement.setInt(7, bandSample.getHeartRate());
            statement.executeUpdate();

        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertRackSensor (RackSensor rack) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO rack_sensor (idSensor, type, alarm) VALUES (?, ?, ?)")
        ){
            statement.setString(1, rack.getRackSensorId());
            statement.setString(2, rack.getType());
            statement.setBoolean(3, rack.getAlarm());
            statement.executeUpdate();
        }
        catch (final SQLIntegrityConstraintViolationException e) {
            System.out.printf("INFO: rack sensor %s as %s sensor already registered in the database.%n", rack.getRackSensorId(), rack.getType());
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRackSensor (RackSensor rackSensor) {
        try (
                PreparedStatement statement = connection.prepareStatement("UPDATE rack_sensor SET alarm = ? WHERE idsensor = ?")
        ){
            statement.setBoolean(1, rackSensor.getAlarm());
            statement.setString(2, rackSensor.getRackSensorId());

            if (statement.executeUpdate() != 1) throw new Exception("ERROR: not valid id " + rackSensor.getRackSensorId());
        }
        catch (final SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void insertTemperatureSample (TemperatureSample sample) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO temperature_samples "
                        + "(idSensor, timestamp, value) VALUES (?, NULL, ?)")
        ){
            statement.setString(1, sample.getId());
            statement.setInt(2, sample.getValue());

            statement.executeUpdate();

        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertHumiditySample (HumiditySample sample) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO humidity_samples "
                        + "(idSensor, timestamp, value) VALUES (?, NULL, ?)")
        ){
            statement.setString(1, sample.getId());
            statement.setInt(2, sample.getValue());

            statement.executeUpdate();

        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOxygenSample (OxygenSample sample) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO oxygen_samples "
                        + "(idSensor, timestamp, value) VALUES (?, NULL, ?)")
        ){
            statement.setString(1, sample.getId());
            statement.setFloat(2, sample.getValue());

            statement.executeUpdate();

        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}
