package it.unipi.dii.iot.mqtt;

public class Topic {
	public static String ALL_BANDS_SAMPLES = "SDCM/band/+/sample";
	public static String ALL_BANDS_STATUS = "SDCM/band/+/status";
    public static String ALL_COMMANDS_TOWARDS_COLLECTOR = "SDCM/collector/+";
    public static String TURN_ON_ALARM = "SDCM/band/%s/alarm";

    
    public static Boolean isBandRegistration(String topic) {
    	 String[] tokens = topic.split("/");
         return tokens[tokens.length - 1].equals("band-registration");
    }
    
    public static Boolean isBandStatus(String topic) {
	   	 String[] tokens = topic.split("/");
	     return tokens[tokens.length - 1].equals("status");
    }
    
    public static Boolean isBandSample(String topic) {
	   	 String[] tokens = topic.split("/");
	     return tokens[tokens.length - 1].equals("sample");
    }
    
    public static String getBandId(String topic) {
    	String[] tokens = topic.split("/");
        return tokens[2];
    }
}
