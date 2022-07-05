#ifndef SMART_ICU_MQTT_MONITOR_CONSTANTS_H
#define SMART_ICU_MQTT_MONITOR_CONSTANTS_H

/* MQTT broker constants. */
#define MQTT_BROKER_IP_ADDRESS                   "fd00::1" /* IPv6 address of the MQTT broker. */
#define MQTT_BROKER_PORT                         1883      /* Port of the MQTT broker. */
#define MQTT_BROKER_KEEP_ALIVE                   60        /* Keep-alive for the MQTT connection. */


/* MQTT band (MQTT client) constants. */
#define MQTT_BAND_ID_LENGTH                           	 46  /* The maximum length of a monitor ID (an IPv6 address). */
#define MQTT_BAND_STATE_CHECK_INTERVAL                	 1   /* Interval in seconds used by the periodic timer to check the internal state. */
#define MQTT_BAND_MAX_TCP_SEGMENT_SIZE                	 256 /* Maximum TCP segment size for the outgoing segments. */
#define MQTT_BAND_INPUT_BUFFER_SIZE                      32  /* Size of the MQTT input buffer. */
#define MQTT_BAND_OUTPUT_BUFFER_SIZE                     256 /* Size of the MQTT output buffer. */
#define MQTT_BAND_TOPIC_MAX_LENGTH                    	 128 /* Maximum length of a topic label. */

/* MQTT band internal states. */
#define MQTT_BAND_STATE_INIT                       		 0 /* Initial state. */
#define MQTT_BAND_STATE_NETWORK_OK		                 1 /* Network is initialized. */
#define MQTT_BAND_STATE_CONNECTING                	 	 2 /* Connecting to the MQTT broker. */
#define MQTT_BAND_STATE_CONNECTED                 	     3 /* Successfully connected to the broker. */
#define MQTT_BAND_STATE_SUBSCRIBING      				 4 /* Subscribing to the topics of interest. */
#define MQTT_BAND_STATE_SUBSCRIBED                    	 5 /* Subscribed to the topics of interest. */
#define MQTT_BAND_STATE_DISCONNECTED    				 6 /* Disconnected from the MQTT broker. */
#define MQTT_BAND_STATE_ACTIVE                     		 7 /* Band weared. */
#define MQTT_BAND_STATE_INACTIVE                         8 /* Band weared. */

/* MQTT command and telemetry topics. */
#define MQTT_BAND_CMD_TOPIC_ALARM_STATE               "cmd/SDCM/%s/patient-state/alarm-state"
#define MQTT_BAND_CMD_TOPIC_BAND_REGISTRATION      	  "cmd/SDCM/collector/monitor-registration"
#define MQTT_BAND_CMD_TOPIC_PATIENT_REGISTRATION      "cmd/SDCM/collector/patient-registration"
#define MQTT_BAND_CMD_TOPIC_STATUS			          "SDCM/band/%s/status"
#define MQTT_BAND_TELEMETRY_TOPIC_BAND_SAMPLE         "SDCM/band/%s/sample"
#define MQTT_BAND_TELEMETRY_TOPIC_ALARM_STATE         "telemetry/SDCM/%s/patient-state/alarm-state"

#endif /* SMART_ICU_MQTT_MONITOR_CONSTANTS_H */
/** @} */
