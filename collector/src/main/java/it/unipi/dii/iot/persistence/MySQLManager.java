package it.unipi.dii.iot.persistence;

import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.model.BandSample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class MySQLManager {

    private Connection connection;

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

    public int updateBand (BandDevice bandDevice) {
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
            return -1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 1;
        }
        return 0;
    }
    
    public void insertBandSample (BandSample bandSample) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO band_samples "
                		+ "(timestamp,bandid,batteryLevel,oxygenSaturation,bloodPressure,temperature,respiration,heartRate)"
                		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
        ){
            statement.setTimestamp(1, bandSample.getTimestamp());
            statement.setString(2, bandSample.getBandId());
            statement.setInt(3, bandSample.getBatteryLevel());
            statement.setInt(4, bandSample.getOxygenSaturation());
            statement.setInt(5, bandSample.getBloodPressure());
            statement.setDouble(6, bandSample.getTemperature());
            statement.setInt(7, bandSample.getRespiration());
            statement.setInt(8, bandSample.getHeartRate());
            statement.executeUpdate();

        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}
