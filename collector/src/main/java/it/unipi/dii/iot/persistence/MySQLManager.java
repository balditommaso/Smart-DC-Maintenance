package it.unipi.dii.iot.persistence;

import it.unipi.dii.iot.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class MySQLManager {

    private Connection connection;

    public MySQLManager (Connection connection) {
        this.connection = connection;
    }

    public void insertVehicle (Vehicle vehicle) {
        try (
                PreparedStatement statement = connection.prepareStatement("INSERT INTO vehicles (id, type, base_station, locked) VALUES (?, ?, ?, ?)")
        ){
            statement.setString(1, vehicle.getId());
            statement.setString(2, vehicle.getType());
            statement.setString(3, vehicle.getBaseStation());
            statement.setBoolean(4, vehicle.getLocked());
            statement.executeUpdate();

        }
        catch (final SQLIntegrityConstraintViolationException e) {
            System.out.printf("INFO: vehicle %s already registered in the database.%n", vehicle.getId());
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateVehicle (Vehicle vehicle) {
        System.out.println("UPDATE");
        try (
                PreparedStatement statement = connection.prepareStatement("UPDATE vehicles SET locked = ? WHERE id = ?")
        ){
            statement.setBoolean(1, vehicle.getLocked());
            statement.setString(2, vehicle.getId());
            if (statement.executeUpdate() != 1) throw new Exception("ERROR: not valid id " + vehicle.getId());
        }
        catch (final SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
