package it.unipi.dii.iot.persistence;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.unipi.dii.iot.config.ConfigParameters;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.*;

public class MySQLDriver {

    private static HikariConfig hikariConfig = new HikariConfig();
    private static HikariDataSource ds;

    static {
        ConfigParameters configParameters = new ConfigParameters("config.properties");
        hikariConfig.setJdbcUrl( "jdbc:mysql://" + configParameters.getDatabaseIp() +
                ":" + configParameters.getDatabasePort() +
                "/" + configParameters.getDatabaseName()
        );
        hikariConfig.setUsername( configParameters.getDatabaseUsername() );
        hikariConfig.setPassword( configParameters.getDatabasePassword() );
        // config.addDataSourceProperty( "cachePrepStmts" , "true" );
        // config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        // config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( hikariConfig );
    }

    private MySQLDriver() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}
