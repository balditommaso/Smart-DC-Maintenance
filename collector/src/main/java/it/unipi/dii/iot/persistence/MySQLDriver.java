package it.unipi.dii.iot.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.Vehicle;

public class MySQLDriver {
	private static MySQLDriver instance = null;

	private static String databaseIp;
	private static int databasePort;
	private static String databaseUsername;
	private static String databasePassword;
	private static String databaseName;
	    
	public static MySQLDriver getInstance() {
	   if(instance == null)
		   instance = new MySQLDriver();

	   return instance;
	}
	
    private MySQLDriver() {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        databaseIp = configParameters.getDatabaseIp();
        databasePort = configParameters.getDatabasePort();
        databaseUsername = configParameters.getDatabaseUsername();
        databasePassword = configParameters.getDatabasePassword();
        databaseName = configParameters.getDatabaseName();
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://"+ databaseIp + ":" + databasePort +
                        "/" + databaseName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                databaseUsername, databasePassword);
    }
    
    public void insertVehicle (Vehicle vehicle) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO vehicles (id, type, base_station, locked) VALUES (?, ?, ?, ?)")
        ){
            statement.setString(1, vehicle.getId());
            statement.setString(2, vehicle.getType());
            statement.setString(3, vehicle.getBaseStation());
            statement.setBoolean(4, vehicle.getLocked());
            statement.executeUpdate();
            
        }
        catch (final SQLIntegrityConstraintViolationException e) {
        	System.out.println(String.format("INFO: vehicle %s already registered in the database.", vehicle.getId()));
        }
        catch (final SQLException e) { 
            e.printStackTrace();
        }
    }
}
