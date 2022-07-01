package it.unipi.dii.iot.persistence;

import it.unipi.dii.iot.model.Band;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class MySQLManager {

    private Connection connection;

    public MySQLManager (Connection connection) {
        this.connection = connection;
    }

    public void insertBand (Band band) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO band (id, active, battery, alter_on) VALUES (?, ?, ?, ?)")
        ){
            statement.setString(1, band.getId());
            statement.setBoolean(2, band.getActive());
            statement.setInt(3, band.getBattery());
            statement.setBoolean(4, band.getAlertOn());
            statement.executeUpdate();

        }
        catch (final SQLIntegrityConstraintViolationException e) {
            System.out.printf("INFO: band %s already registered in the database.%n", band.getId());
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateBand (Band band) {
        System.out.println("UPDATE");
        try (
                PreparedStatement statement = connection.prepareStatement("UPDATE band SET active = ? WHERE id = ?")
        ){
            statement.setBoolean(1, band.getActive());
            statement.setString(2, band.getId());
            if (statement.executeUpdate() != 1) throw new Exception("ERROR: not valid id " + band.getId());
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
}
