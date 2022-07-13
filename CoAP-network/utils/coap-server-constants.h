/* COAP Collector Server */
#define SERVER_IP                           "fd00::1"
#define SERVER_PORT                         5683
#define SERVER_SERVICE                      "/registration"

#define RACK_SENSOR                         "rack_sensor"
#define OXYGEN_SENSOR                       "oxygen_sensor"

/* COAP constants. */
#define COAP_ID_LENGTH                           	 46  /* The maximum length of a monitor ID (an IPv6 address). */
#define COAP_STATE_CHECK_INTERVAL                	 1   /* Interval in seconds used by the periodic timer to check the internal state. */
#define COAP_CHUNK_SIZE                	             64 /* Maximum TCP segment size for the outgoing segments. */
#define COAP_URL_SIZE                                64
#define COAP_BUFFER_SIZE                             132  /* Size of the MQTT input buffer. */

/* COAP internal states. */
#define COAP_STATE_INIT                       		 0 /* Initial state. */
#define COAP_CHECK_CONNCTION                         1
#define COAP_STATE_NETWORK_OK		                 2 /* Network is initialized. */
#define COAP_STATE_ACTIVE                     		 3 /* Band weared. */

/* COAP resources path */
#define COAP_TEMPERATURE_PATH               "rack/temperature"
#define COAP_HUMIDITY_PATH      	        "rack/humidity"
#define COAP_OXYGEN_PATH                    "sensor/oxygen"

#define TEMPERATURE_INIT                        20
#define HUMIDITY_INIT                           55
#define OXYGEN_INIT                             16