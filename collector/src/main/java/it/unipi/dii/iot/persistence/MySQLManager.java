package it.unipi.dii.iot.persistence;

import it.unipi.dii.iot.model.BandDevice;
import it.unipi.dii.iot.model.BandHeartbeat;

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
                PreparedStatement statement = connection.prepareStatement("INSERT INTO band_device (idband, active, alert_on) VALUES (?, ?, ?)")
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
        System.out.println("UPDATE");
        try (
                PreparedStatement statement = connection.prepareStatement("UPDATE band SET active = ? WHERE idband = ?")
        ){
            statement.setBoolean(1, bandDevice.getActive());
            statement.setString(2, bandDevice.getId());
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
    
    public void insertBandHeartbeat (BandHeartbeat bandHeartbeat) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO band_heartbeat (idband, heart_frequency, battery_level) VALUES (?, ?, ?)")
        ){
            statement.setString(1, bandHeartbeat.getDeviceId());
            statement.setInt(2, bandHeartbeat.getHeartFrequency());
            statement.setInt(3, bandHeartbeat.getBatteryLevel());
            statement.executeUpdate();

        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}
