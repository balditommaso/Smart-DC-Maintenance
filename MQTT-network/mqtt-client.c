#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"
#include "./band-samples/band-sample.h"
#include "./utils/json-message.h"
#include "./utils/mqtt-client-constants.h"

#include <string.h>
#include <strings.h>

/*---------------------------------------------------------------------------*/
#define LOG_MODULE "mqtt-client"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_client_process);
AUTOSTART_PROCESSES(&mqtt_client_process);
PROCESS(mqtt_client_process, "MQTT Client");

struct mqtt_band {
    char band_id[MQTT_BAND_ID_LENGTH];
    int battery_level;

    /* Internal state. */
    clock_time_t state_check_interval;
    struct etimer state_check_timer;
    uint8_t state;

    struct mqtt_module {
        struct mqtt_connection connection;
        mqtt_status_t status;
    } mqtt_module;
  	
  /* Buffers used to store the topics and the output messages. */
  	char topic_buffer[MQTT_BAND_TOPIC_MAX_LENGTH];
  	char output_buffer[MQTT_BAND_OUTPUT_BUFFER_SIZE];
};

static struct mqtt_band band;
  
/*---------------------------------------------------------------------------*/

static bool have_connectivity(void)
{
    if (uip_ds6_get_global(ADDR_PREFERRED) == NULL || uip_ds6_defrt_choose() == NULL) 
    {
        return false;
    }
    return true;
}

/*---------------------------------------------------------------------------*/
static void publish(char *topic, char* output_buffer)
{
    LOG_INFO("Publishing %s in the topic %s.\n", output_buffer, topic);
    band.mqtt_module.status = mqtt_publish(&band.mqtt_module.connection,
                                            NULL,
                                            topic,
                                            (uint8_t *)output_buffer,
                                            strlen(output_buffer),
                                            MQTT_QOS_LEVEL_0,
                                            MQTT_RETAIN_OFF);
    switch(band.mqtt_module.status) 
    {
        case MQTT_STATUS_OK:
            return;
        case MQTT_STATUS_NOT_CONNECTED_ERROR: {
            LOG_ERR("Publishing failed. Error: MQTT_STATUS_NOT_CONNECTED_ERROR.\n");
            return;
        }
        case MQTT_STATUS_OUT_QUEUE_FULL: {
            LOG_ERR("Publishing failed. Error: MQTT_STATUS_OUT_QUEUE_FULL.\n");
            break;
        }
        default:
            LOG_ERR("Publishing failed. Error: unknown.\n");
            return;
    }
}

/*---------------------------------------------------------------------------*/
static void pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk, uint16_t chunk_len)
{
    LOG_INFO("Received %s in the topic %s.\n", chunk, topic);
		if(band.state != MQTT_BAND_STATE_ACTIVE) 
        {
            LOG_INFO("Discarding the MQTT message. The band is not active.\n");
            return;
		}
		
    snprintf(band.topic_buffer, MQTT_BAND_TOPIC_MAX_LENGTH, MQTT_BAND_TOPIC_ALERT_STATE, band.band_id);
    LOG_INFO("The topic %s.\n", band.topic_buffer);
  	if(!strcmp(band.topic_buffer, (char*)topic)) {
    	LOG_INFO("Starting the alert.\n");
    	band.state = MQTT_BAND_STATE_ALERT_ON;
    	return;
  	}

    LOG_INFO("Discarding the MQTT message: bad format.\n");
}

/*---------------------------------------------------------------------------*/
static void mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
    switch (event) 
    {
        case MQTT_EVENT_CONNECTED: 
        {
            LOG_INFO("Application has a MQTT connection\n");
            band.state = MQTT_BAND_STATE_CONNECTED;
            break;
        }
        case MQTT_EVENT_DISCONNECTED: 
        {
            LOG_INFO("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));
            band.state = MQTT_BAND_STATE_DISCONNECTED;
            process_poll(&mqtt_client_process);
            break;
        }
        case MQTT_EVENT_PUBLISH: 
        {
            struct mqtt_message* msg_ptr = data;
            pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                        msg_ptr->payload_chunk, msg_ptr->payload_length);
            break;
        }
        case MQTT_EVENT_SUBACK: 
        {
#if MQTT_311
    mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

    if(suback_event->success) {
      	LOG_INFO("Application is subscribed to topic successfully\n");
      	band.state = MQTT_BAND_STATE_SUBSCRIBED;
    } else {
      	LOG_ERR("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
       	band.state = MQTT_BAND_STATE_CONNECTED; /* Go back to the previous state and retry. */
    }
	#else
    LOG_INFO("Application is subscribed to topic successfully\n");
    band.state = MQTT_BAND_STATE_SUBSCRIBED;
#endif
            break;
        }
        case MQTT_EVENT_UNSUBACK: 
        {
            LOG_INFO("Application is unsubscribed to topic successfully\n");
            break;
        }
        case MQTT_EVENT_PUBACK: 
        {
            LOG_INFO("Publishing complete.\n");
            break;
        }
        default:
            LOG_ERR("Application got a unhandled MQTT event: %i\n", event);
            break;
    }
}

/*---------------------------------------------------------------------------*/
static void handle_state_init(void)
{
	band.battery_level = 100;
	
    if(have_connectivity()) {
        LOG_INFO("Connected to the network. ");
        LOG_INFO_("Global address: ");
        LOG_INFO_6ADDR(&(uip_ds6_get_global(ADDR_PREFERRED)->ipaddr));
        LOG_INFO_(". Link local address: ");
        LOG_INFO_6ADDR(&(uip_ds6_get_link_local(ADDR_PREFERRED)->ipaddr));
        LOG_INFO_("\n");
        band.state = MQTT_BAND_STATE_NETWORK_OK;
    } else {
        LOG_INFO("Connecting to the network.\n");
    }
}

/*---------------------------------------------------------------------------*/
static bool handle_state_network_ok(void)
{
    /* Initialize the band ID as the global IPv6 address. */
    uiplib_ipaddr_snprint(band.band_id,
                        MQTT_BAND_ID_LENGTH,
                        &(uip_ds6_get_global(ADDR_PREFERRED)->ipaddr));
                            
    
    /* Initialize MQTT engine. */
    mqtt_register(&band.mqtt_module.connection,
                &mqtt_client_process,
                band.band_id,
                mqtt_event,
                MQTT_BAND_MAX_TCP_SEGMENT_SIZE);
    LOG_INFO("MQTT engine initialized. Band id: %s.\n", band.band_id);
    
    /* Connect to the broker. */
    LOG_INFO("Connecting to the MQTT broker at %s, %d.\n", MQTT_BROKER_IP_ADDRESS, MQTT_BROKER_PORT);
    band.mqtt_module.status = mqtt_connect(&band.mqtt_module.connection,
                                            MQTT_BROKER_IP_ADDRESS,
                                            MQTT_BROKER_PORT,
                                            MQTT_BROKER_KEEP_ALIVE,
                                            MQTT_CLEAN_SESSION_ON);

    if(band.mqtt_module.status == MQTT_STATUS_ERROR) {
        LOG_ERR("Error while connecting to the MQTT broker: invalid IP address\n");
        return false;
    }
  
  
    band.state = MQTT_BAND_STATE_CONNECTING;
    return true;

}

static bool handle_state_connected(void) 
{    
    /* Subscribe to the topic of alarm commands sent by the collector. */
    snprintf(band.topic_buffer, MQTT_BAND_TOPIC_MAX_LENGTH, MQTT_BAND_TOPIC_ALERT_STATE, band.band_id);
        
    LOG_INFO("Subscribing to the topic %s.\n", band.topic_buffer);
    band.mqtt_module.status = mqtt_subscribe(&band.mqtt_module.connection,
                                            NULL,
                                            band.topic_buffer,
                                            MQTT_QOS_LEVEL_0);
                                              
    if(band.mqtt_module.status != MQTT_STATUS_OK) {
        LOG_ERR("Failed to subscribe to the topic %s.\n", band.topic_buffer);
        return false;
    }

    band.state = MQTT_BAND_STATE_SUBSCRIBING;
    return true;                                       
}

/*---------------------------------------------------------------------------*/
static void handle_state_subscribed(void)
{
	/* Register the band sending a message to the collector. */
    set_json_msg_band_registration( band.output_buffer,
                                    MQTT_BAND_OUTPUT_BUFFER_SIZE,
                                    band.band_id);
                                    
    snprintf(band.topic_buffer, MQTT_BAND_TOPIC_MAX_LENGTH, "%s", MQTT_BAND_TOPIC_BAND_REGISTRATION);
    publish(band.topic_buffer, band.output_buffer);
    
    leds_off(LEDS_ALL);
    leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
        
	band.state = MQTT_BAND_STATE_INACTIVE;
}

/*---------------------------------------------------------------------------*/
static void handle_button_press(button_hal_button_t *button)
{
	snprintf(band.topic_buffer, MQTT_BAND_TOPIC_MAX_LENGTH, MQTT_BAND_TOPIC_STATUS, band.band_id);

    if (band.state == MQTT_BAND_STATE_INACTIVE) {
        LOG_INFO("Band %s activated.\n", band.band_id);
            
        leds_off(LEDS_ALL);
        leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
        
        set_json_msg_status(band.output_buffer, MQTT_BAND_OUTPUT_BUFFER_SIZE, true);
        publish(band.topic_buffer, band.output_buffer);
        
        init_sample_values();
        band.state = MQTT_BAND_STATE_ACTIVE;
    }
    else if (band.state == MQTT_BAND_STATE_ACTIVE || band.state == MQTT_BAND_STATE_BATTERY_LOW) {						
        LOG_INFO("Band %s disactivated. Charging\n", band.band_id);
        
       	band.battery_level = (band.battery_level < 100)?band.battery_level+1:100;
                    
        leds_off(LEDS_ALL);
        leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
        
        set_json_msg_status(band.output_buffer, MQTT_BAND_OUTPUT_BUFFER_SIZE, false);
        publish(band.topic_buffer, band.output_buffer);
        
        band.state = MQTT_BAND_STATE_INACTIVE;
    }
    else if (band.state == MQTT_BAND_STATE_ALERT_ON) {
    	LOG_INFO("Band %s Alert stopped.\n", band.band_id);
    	
    	leds_off(LEDS_ALL);
        leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
        
        set_json_msg_alert_stopped(band.output_buffer, MQTT_BAND_OUTPUT_BUFFER_SIZE);
        publish(band.topic_buffer, band.output_buffer);
    	
    	band.state = MQTT_BAND_STATE_INACTIVE;
    }
}

/*---------------------------------------------------------------------------*/
static void handle_state_active()
{
	band.battery_level -= 1;
 	int oxygen_saturation = get_oxygen_saturation();
 	int blood_pressure = get_blood_pressure();
  	double	temperature = get_temperature();
  	int respiration = get_respiration();
 	int heart_rate = get_heart_rate();
 	
 	set_json_msg_band_sample(band.output_buffer, MQTT_BAND_OUTPUT_BUFFER_SIZE, 
 							band.battery_level,
 							oxygen_saturation, 
 							blood_pressure, 
 							temperature,
 							respiration,
 							heart_rate);						
 							
 	snprintf(band.topic_buffer, MQTT_BAND_TOPIC_MAX_LENGTH, MQTT_BAND_TOPIC_BAND_SAMPLE, band.band_id);
 	publish(band.topic_buffer, band.output_buffer);
  
  	// Check if battery level low
    if(band.battery_level < BATTERY_LEVEL_LOW) {
    	LOG_INFO("Alarming Battery Level too low: %d. Sensors stopped, recharge the band\n", band.battery_level);
    	leds_on(LEDS_ALL);
    	band.state = MQTT_BAND_STATE_BATTERY_LOW;
    }
}

/*---------------------------------------------------------------------------*/
static void handle_state_inactive()
{
	band.battery_level += 3;
	if (band.battery_level > 100) band.battery_level = 100;
}

/*---------------------------------------------------------------------------*/
static void handle_state_alert_on() 
{
	// Leds Blinking
    static bool alternate = false;
    alternate = !alternate;
    
    leds_off(LEDS_ALL);
    	
    if (alternate) 
    	leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
    else 
    	leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
}

/*---------------------------------------------------------------------------*/
static void init_band() 
{
	band.state = MQTT_BAND_STATE_INIT;
	
	// Initialize the periodic timer to check the internal state. 
    band.state_check_interval = MQTT_BAND_STATE_CHECK_INTERVAL*CLOCK_SECOND;
    etimer_set(&band.state_check_timer, band.state_check_interval);
}

/*---------------------------------------------------------------------------*/
static void finish_band()
{
    etimer_stop(&band.state_check_timer);
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(mqtt_client_process, ev, data)
{
    PROCESS_BEGIN();
    LOG_INFO("Process started.\n");
	init_band();
	
    /* Main loop */
    while(1) 
    {
        PROCESS_YIELD();

        if ((ev == PROCESS_EVENT_TIMER && data == &band.state_check_timer) || 
            (ev == PROCESS_EVENT_POLL)) 
        {
			if (band.state == MQTT_BAND_STATE_INIT) {
				handle_state_init();
			} 
            if (band.state == MQTT_BAND_STATE_NETWORK_OK) {
            	bool success = handle_state_network_ok();
            	if(!success) break;
            }
            if (band.state == MQTT_BAND_STATE_CONNECTED) {
              bool success = handle_state_connected();
            	if(!success) break;
            }
            if (band.state == MQTT_BAND_STATE_SUBSCRIBED) {
            	handle_state_subscribed();
            }
			if (band.state == MQTT_BAND_STATE_ACTIVE) {
				handle_state_active();
			}
			if (band.state == MQTT_BAND_STATE_INACTIVE) {
				handle_state_inactive();
			}
			if (band.state == MQTT_BAND_STATE_ALERT_ON) {
				handle_state_alert_on();
			}
			if (band.state == MQTT_BAND_STATE_DISCONNECTED) {
				LOG_ERR("Disconnected form MQTT broker\n");	
				band.state = MQTT_BAND_STATE_INIT;
     			  break;
			}
			etimer_reset(&band.state_check_timer);
			continue;
		}
					
		if (ev == button_hal_press_event &&
		    (MQTT_BAND_STATE_ACTIVE || MQTT_BAND_STATE_INACTIVE || MQTT_BAND_STATE_BATTERY_LOW)) {
			handle_button_press((button_hal_button_t *)data);
		    continue;
    	}
    }
  	
  	finish_band();
  	LOG_INFO("Stopping the process.\n");
  	PROCESS_END();
}
