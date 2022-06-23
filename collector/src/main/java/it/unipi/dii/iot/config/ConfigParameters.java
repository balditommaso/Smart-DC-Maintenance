package it.unipi.dii.iot.config;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigParameters {   
    private String databaseIp;
    private int databasePort;
    private String databaseUsername;
    private String databasePassword;
    private String databaseName;
    
    public ConfigParameters(String configFilePath) {
    	try{
            FileInputStream fis = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(fis);
            
            databaseIp = prop.getProperty("databaseIp");
            databasePort = Integer.parseInt(prop.getProperty("databasePort"));
            databaseUsername = prop.getProperty("databaseUsername");
            databasePassword = prop.getProperty("databasePassword");
            databaseName = prop.getProperty("databaseName");
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        validate();
    }
    
    private void validate() {
    	
    }
    
    public String getDatabaseIp() { return databaseIp; }
    public Integer getDatabasePort() { return databasePort; }
    public String getDatabaseUsername() { return databaseUsername; }
    public String getDatabasePassword() { return databasePassword; }
    public String getDatabaseName() { return databaseName; }
}
