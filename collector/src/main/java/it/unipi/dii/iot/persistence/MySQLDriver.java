package it.unipi.dii.iot.persistence;


import it.unipi.dii.iot.config.ConfigParameters;
import it.unipi.dii.iot.model.Vehicle;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.*;

public class MySQLDriver {
    private static MySQLDriver instance = null;
    private static String databaseIp;
    private static int databasePort;
    private static String databaseUsername;
    private static String databasePassword;
    private static String databaseName;
    //private DataSource dataSource = null;

    private MySQLDriver() {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        databaseIp = configParameters.getDatabaseIp();
        databasePort = configParameters.getDatabasePort();
        databaseUsername = configParameters.getDatabaseUsername();
        databasePassword = configParameters.getDatabasePassword();
        databaseName = configParameters.getDatabaseName();
        // dataSource = createDataSource();
    }
    /*
    private DataSource createDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://"+ databaseIp + ":" + databasePort + "/" + databaseName);
        ds.setUsername(databaseUsername);
        ds.setPassword(databasePassword);

        return ds;
    }
    */

	public static MySQLDriver getInstance() {
        if(instance == null)
            instance = new MySQLDriver();

        return instance;
	}

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://"+ databaseIp + ":" + databasePort +
                        "/" + databaseName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                databaseUsername, databasePassword);
    }
    
    public void insertVehicle (Vehicle vehicle) {
        try (
                Connection connection = getConnection();
                //Connection connection = dataSource.getConnection();
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
        try (
                Connection connection = getConnection();
                //Connection connection = dataSource.getConnection();
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
