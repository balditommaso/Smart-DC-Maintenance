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
static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_client_process);
AUTOSTART_PROCESSES(&mqtt_client_process);

struct mqtt_band {
  char band_id[MQTT_BAND_ID_LENGTH];
  struct alarm_system alarm;

  /* Internal state. */
  clock_time_t state_check_interval;
  struct etimer state_check_timer;
  uint8_t state;

  /* ID of the patient currently attached to the monitor. */
//  char patient_id[MQTT_MONITOR_PATIENT_ID_LENGTH];
  
  struct mqtt_module {
    struct mqtt_connection connection;
    mqtt_status_t status;
  } mqtt_module;
  
  /* Buffers used to store the topics regarding commands. */
  struct cmd_topics {
    char user_registration[MQTT_BAND_TOPIC_MAX_LENGTH];
    char band_registration[MQTT_BAND_TOPIC_MAX_LENGTH];
    char alarm_state[MQTT_BAND_TOPIC_MAX_LENGTH];
  } cmd_topics;

  /* Buffers used to store the topics regarding telemetry data. */
  struct telemetry_topics {
    char oxygen_saturation[MQTT_BAND_TOPIC_MAX_LENGTH];
    char blood_pressure[MQTT_BAND_TOPIC_MAX_LENGTH];
    char temperature[MQTT_BAND_TOPIC_MAX_LENGTH];
    char respiration[MQTT_BAND_TOPIC_MAX_LENGTH];
    char alarm_state[MQTT_BAND_TOPIC_MAX_LENGTH];
    char heart_rate[MQTT_BAND_TOPIC_MAX_LENGTH];
  } telemetry_topics;
  
  /* Buffers used to store the output messages. */
  struct output_buffers {
    char patient_registration[MQTT_BAND_OUTPUT_BUFFER_SIZE];
    char monitor_registration[MQTT_BAND_OUTPUT_BUFFER_SIZE];
    char oxygen_saturation[MQTT_BAND_OUTPUT_BUFFER_SIZE];
    char blood_pressure[MQTT_BAND_OUTPUT_BUFFER_SIZE];
    char temperature[MQTT_BAND_OUTPUT_BUFFER_SIZE];
    char respiration[MQTT_BAND_OUTPUT_BUFFER_SIZE];
    char alarm_state[MQTT_BAND_OUTPUT_BUFFER_SIZE];
    char heart_rate[MQTT_BAND_OUTPUT_BUFFER_SIZE];
  } output_buffers; 
};

static struct mqtt_band band;
  

// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct ctimer blinking_timer;
// global variable for the application
#define ALERT_BLINK     3
#define BLINK_PERIOD    2
// static bool alert_on = false;
static bool active = false;
static int blinking_count = 0;
// static int battery_lvl = 100;

/*---------------------------------------------------------------------------*/

static bool have_connectivity(void)
{
    if (uip_ds6_get_global(ADDR_PREFERRED) == NULL || uip_ds6_defrt_choose() == NULL) 
    {
        return false;
    }
    return true;
}

// utility function
static void blinking(void *ptr)
{
    if (blinking_count < ALERT_BLINK*2) 
    {
        blinking_count++;
        leds_single_toggle(LEDS_RED);
        ctimer_reset(&blinking_timer);
    } 
    else 
    {
        blinking_count = 0;
    }
}


static void init_topics(void)
{
  /* Command topics. */
  snprintf(band.cmd_topics.alarm_state, MQTT_MONITOR_TOPIC_MAX_LENGTH, MQTT_MONITOR_CMD_TOPIC_ALARM_STATE, monitor.monitor_id);
  snprintf(band.cmd_topics.monitor_registration, MQTT_MONITOR_TOPIC_MAX_LENGTH, "%s", MQTT_MONITOR_CMD_TOPIC_MONITOR_REGISTRATION);
  snprintf(band.cmd_topics.patient_registration, MQTT_MONITOR_TOPIC_MAX_LENGTH, "%s",MQTT_MONITOR_CMD_TOPIC_PATIENT_REGISTRATION);

	/* Telemetry topics. */
  snprintf(monitor.telemetry_topics.heart_rate, MQTT_MONITOR_TOPIC_MAX_LENGTH, MQTT_MONITOR_TELEMETRY_TOPIC_HEART_RATE, monitor.monitor_id);
  snprintf(monitor.telemetry_topics.blood_pressure, MQTT_MONITOR_TOPIC_MAX_LENGTH, MQTT_MONITOR_TELEMETRY_TOPIC_BLOOD_PRESSURE, monitor.monitor_id);
  snprintf(monitor.telemetry_topics.temperature, MQTT_MONITOR_TOPIC_MAX_LENGTH, MQTT_MONITOR_TELEMETRY_TOPIC_TEMPERATURE, monitor.monitor_id);
	snprintf(monitor.telemetry_topics.respiration, MQTT_MONITOR_TOPIC_MAX_LENGTH, MQTT_MONITOR_TELEMETRY_TOPIC_RESPIRATION, monitor.monitor_id);
  snprintf(monitor.telemetry_topics.oxygen_saturation, MQTT_MONITOR_TOPIC_MAX_LENGTH, MQTT_MONITOR_TELEMETRY_TOPIC_OXYGEN_SATURATION, monitor.monitor_id);
  snprintf(monitor.telemetry_topics.alarm_state, MQTT_MONITOR_TOPIC_MAX_LENGTH, MQTT_MONITOR_TELEMETRY_TOPIC_ALARM_STATE, monitor.monitor_id);

	LOG_DBG("Command alarm state topic: %s\n", monitor.cmd_topics.alarm_state);
  LOG_DBG("Command monitor registration topic: %s\n", monitor.cmd_topics.monitor_registration);
  LOG_DBG("Command patient registration topic: %s\n", monitor.cmd_topics.patient_registration);
  LOG_DBG("Telemetry heart rate topic: %s\n", monitor.telemetry_topics.heart_rate);
  LOG_DBG("Telemetry blood pressure topic: %s\n", monitor.telemetry_topics.blood_pressure);
  LOG_DBG("Telemetry temperature topic: %s\n", monitor.telemetry_topics.temperature);
  LOG_DBG("Telemetry respiration topic: %s\n", monitor.telemetry_topics.respiration);
  LOG_DBG("Telemetry oxygen saturation topic: %s\n", monitor.telemetry_topics.oxygen_saturation);
  LOG_DBG("Telemetry alarm state topic: %s\n", monitor.telemetry_topics.alarm_state);
}

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_process, "MQTT Client");

/*---------------------------------------------------------------------------*/
static void publish(char *topic, char* output_buffer)
{
  LOG_INFO("Publishing %s in the topic %s.\n", output_buffer, topic);
  monitor.mqtt_module.status = mqtt_publish(&band.mqtt_module.connection,
                                            NULL,
                                            topic,
                                            (uint8_t *)output_buffer,
                                            strlen(output_buffer),
                                            MQTT_QOS_LEVEL_0,
                                            MQTT_RETAIN_OFF);
  switch(monitor.mqtt_module.status) {
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

static void pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk, uint16_t chunk_len)
{
    LOG_INFO("Received %s in the topic %s.\n", chunk, topic);

		if(band.state != MQTT_BAND_STATE_ACTIVE) {
		  LOG_INFO("Discarding the MQTT message. The band is not active.\n");
		  return;
		}
		
    char start_alamr_msg[MQTT_BAND_INPUT_BUFFER_SIZE];
    json_message_alarm_started(start_alarm_msg, MQTT_MONITOR_INPUT_BUFFER_SIZE);
    
  	if(strcmp(start_alarm_msg, (char*)msg->payload_chunk) == 0) {
    	LOG_INFO("Starting the alarm.\n");
    	alarm_start(&monitor.alarm); /* There is no need to notify the collector about the state change. */
    	ctimer_set(&blinking_timer, CLOCK_SECOND * BLINK_PERIOD, blinking, NULL);
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
            printf("Application has a MQTT connection\n");
            band.state = MQTT_BAND_STATE_CONNECTED;
            break;
        }
        case MQTT_EVENT_DISCONNECTED: 
        {
            printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));
            band.state = MQTT_BAND_STATE_DISCONNECTED;
            process_poll(&mqtt_client_process);
            break;
        }
        case MQTT_EVENT_PUBLISH: 
        {
            msg_ptr = data;
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

static void handle_state_init(void)
{
  if(have_connectivity()) {
    LOG_INFO("Connected to the network. ");
    LOG_INFO_("Global address: ");
    LOG_INFO_6ADDR(&(uip_ds6_get_global(ADDR_PREFERRED)->ipaddr));
    LOG_INFO_(". Link local address: ");
    LOG_INFO_6ADDR(&(uip_ds6_get_link_local(ADDR_PREFERRED)->ipaddr));
    LOG_INFO_("\n");
    monitor.state = MQTT_BAND_STATE_NETWORK_OK;
  } else {
    LOG_INFO("Connecting to the network.\n");
  }
}

static bool handle_state_network_ok(void)
{
	/* Initialize the band ID as the global IPv6 address. */
  uiplib_ipaddr_snprint(band.band_id,
                        MQTT_BAND_ID_LENGTH,
                        &(uip_ds6_get_global(ADDR_PREFERRED)->ipaddr));
                        
  
  /* Initialize MQTT engine. */
  mqtt_register(&bands.mqtt_module.connection,
                &mqtt_client_process,
                band.band_id,
                handle_mqtt_event,
                MQTT_BAND_MAX_TCP_SEGMENT_SIZE);
  LOG_INFO("MQTT engine initialized. Band id: %s.\n", band.band_id);
  
   /* Connect to the broker. */
  LOG_INFO("Connecting to the MQTT broker at %s, %d.\n", MQTT_BAND_BROKER_IP_ADDRESS, MQTT_BAND_BROKER_PORT);
  monitor.mqtt_module.status = mqtt_connect(&monitor.mqtt_module.connection,
                                            MQTT_BAND_BROKER_IP_ADDRESS,
                                            MQTT_BAND_BROKER_PORT,
                                            MQTT_BAND_BROKER_KEEP_ALIVE,
                                            MQTT_CLEAN_SESSION_ON);

  if(band.mqtt_module.status == MQTT_STATUS_ERROR) {
    LOG_ERR("Error while connecting to the MQTT broker: invalid IP address\n");
    return false;
  }
  
  
  band.state = MQTT_BAND_STATE_CONNECTING;
  return true;
  
//  mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
 //                          (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
}

static bool handle_state_connected(void) 
{
  /* Initialize the topics, using the monitor ID. */
  init_topics();
  
  /* Subscribe to the topic of alarm commands sent by the collector. */
  LOG_INFO("Subscribing to the topic %s.\n", band.cmd_topics.alarm_state);
  band.mqtt_module.status = mqtt_subscribe(&band.mqtt_module.connection,
                                              NULL,
                                              band.cmd_topics.alarm_state,
                                              MQTT_QOS_LEVEL_0);
                                              
  if(band.mqtt_module.status != MQTT_STATUS_OK) {
    LOG_ERR("Failed to subscribe to the topic %s.\n", band.cmd_topics.alarm_state);
    return false;
  }

  band.state = MQTT_BAND_STATE_SUBSCRIBING;
  return true;                                       
}

static void handle_state_subscribed(void)
{
/* Register the monitor sending a message to the collector. */
  json_message_monitor_registration(band.output_buffers.band_registration,
                                    MQTT_BAND_OUTPUT_BUFFER_SIZE,
                                    band.band_id);
  publish(band.cmd_topics.band_registration, band.output_buffers.band_registration);

  /* Start the sensor processes (without starting the sampling activity). */
  sensors_cmd_start_processes();
  
	/* Initialize the alarm system. */
  alarm_init(&monitor.alarm);

  /*
   * From this point on, monitor.state_check_timer is used only to check
   * for eventual disconnections (state MQTT_MONITOR_STATE_DISCONNECTED).
   */
  band.state = MQTT_BAND_STATE_WAITING_PATIENT_ID;
  LOG_INFO("Waiting for a new patient ID on the serial line.\n");

#ifdef AUTOMATIC_PATIENT_ID_CONFIGURATION
  LOG_INFO("Automatic configuration of the new patient ID.\n");
  char random_patient_ID[MQTT_MONITOR_PATIENT_ID_LENGTH];
  snprintf(random_patient_ID, MQTT_MONITOR_PATIENT_ID_LENGTH, "auto_%d", rand());
  handle_new_patient_ID(random_patient_ID);
#endif
}


static void handle_button_press(button_hal_button_t *button)
{
	LOG_INFO("Button press event: %d s.\n", button->press_duration_seconds);
	
/*

                sprintf(pub_topic, "band/%s/", client_id);
                
                // active
                if (ev == button_hal_press_event) 
                {
                    strcat(pub_topic, "status");
                    if (!active) 
                    {
                        leds_off(LEDS_ALL);
                        leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
                        sprintf(app_buffer, "%s", "active");
                    } 
                    else 
                    {
                        leds_off(LEDS_ALL);
                        leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
                        sprintf(app_buffer, "%s", "disactive");
                    }

                    active = !active;
                    LOG_INFO("Status: %s\n", app_buffer);
                    mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
                        strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
                }
                // battery

                // heart-rate
                


*/
}

static void init_band() 
{
	band.state = MQTT_BAND_STATE_STARTED;
	
	/* Initialize the periodic timer to check the internal state. */
  band.state_check_interval = MQTT_BAND_STATE_CHECK_INTERVAL*CLOCK_SECOND;
  etimer_set(&band.state_check_timer, band.state_check_interval);

}

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
    while(1) {

        PROCESS_YIELD();

        if ((ev == PROCESS_EVENT_TIMER && data == &band.state_check_timer) || 
            (ev == PROCESS_EVENT_POLL)) {
			  			  
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

						if(band.state == MQTT_BAND_STATE_DISCONNECTED) {
							LOG_ERR("Disconnected form MQTT broker\n");	
						  break;
						}

						etimer_reset(&band.state_check_timer);
						continue;
					}
					
				if(ev == button_hal_periodic_event && band.state == MQTT_BAND_STATE_OPERATIONAL) {
				  handle_button_press((button_hal_button_t *)data);
				  continue;
    		}
    	}
  	}
  	
  	finish_band();
  	LOG_INFO("Stopping the process.\n");
  	PROCESS_END();
}
