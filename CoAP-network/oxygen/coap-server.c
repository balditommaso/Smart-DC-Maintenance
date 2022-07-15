
#include "contiki.h"
#include "coap-engine.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "dev/leds.h"
#include "coap-blocking-api.h"

#include "node-id.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "net/routing/routing.h"

#include "../sensor-signs/sensor-sample.h"
#include "../utils/coap-server-constants.h"
#include "../utils/json-message.h"

#include <stdlib.h>
#include <string.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Oxygen Node"
#define LOG_LEVEL LOG_LEVEL_APP

struct coap_rack {
    clock_time_t state_check_interval;
    struct etimer state_check_timer;
    uint8_t state;
};


PROCESS_NAME(contiki_coap_server);
AUTOSTART_PROCESSES(&contiki_coap_server);
PROCESS(contiki_coap_server, "CoAP Server");


static struct coap_rack rack;

static void init_rack_state()
{
    rack.state_check_interval = COAP_STATE_CHECK_INTERVAL * CLOCK_SECOND;
    rack.state = COAP_STATE_INIT;
    etimer_set(&rack.state_check_timer, rack.state_check_interval);
}

void client_chunk_handler(coap_message_t *response)
{
    if (response == NULL) 
    {
        LOG_INFO("request timed out\n");
        return;
    }

    const uint8_t *chunk;
    int len = coap_get_payload(response, &chunk);

    LOG_INFO("Received response: %s\n", (char*)chunk);
    if (parse_json_registration((char*)chunk, (size_t)len))
        rack.state = COAP_STATE_ACTIVE;
}

static void is_connected() 
{
    if (NETSTACK_ROUTING.node_is_reachable()) 
    {
        LOG_INFO("The Border Router is reachable\n");
        rack.state = COAP_STATE_NETWORK_OK;
    }
    else 
    {
        LOG_INFO("Waiting for connection with the Border Router\n");
    }
    
}

static void finish_rack() 
{
    etimer_stop(&rack.state_check_timer);
}

extern coap_resource_t res_oxygen, res_o2_ventilation;

PROCESS_THREAD(contiki_coap_server, ev, data) 
{
    PROCESS_BEGIN();
    LOG_INFO("Process Started");
    // rack sensor not active yet
    init_rack_state();
    leds_single_on(LEDS_RED);
    // activate the resource
    LOG_INFO("Starting the rack-sensor CoAP Server\n");
    coap_activate_resource(&res_oxygen, COAP_OXYGEN_PATH);
    coap_activate_resource(&res_o2_ventilation, COAP_VENTILATION_PATH);

    static coap_endpoint_t server_endpoint;
    static coap_message_t request[1];

    while(1) 
    {
        PROCESS_YIELD();

        if ((ev == PROCESS_EVENT_TIMER && data == &rack.state_check_timer) || 
            (ev == PROCESS_EVENT_POLL))
        {
            if (rack.state == COAP_STATE_INIT)
            {
                leds_single_on(LEDS_RED);
                is_connected();
            }
            else if (rack.state == COAP_STATE_NETWORK_OK)
            {
                LOG_INFO("Sending registration message\n");
                leds_single_toggle(LEDS_RED);
                char url[COAP_URL_SIZE];
                sprintf(url, "coap://[%s]:%d", SERVER_IP, SERVER_PORT);
                LOG_INFO("Try to register to %s\n", url);
                coap_endpoint_parse(url, strlen(url), &server_endpoint);      

                // prepare the message
                char message[COAP_CHUNK_SIZE];
                set_json_msg_sensor_registration(message, COAP_CHUNK_SIZE, COAP_OXYGEN_PATH);
                coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
                coap_set_header_uri_path(request, SERVER_SERVICE);
                coap_set_payload(request, (uint8_t*)message, sizeof(message)-1);

                // send request of registration
                COAP_BLOCKING_REQUEST(&server_endpoint, request, client_chunk_handler);
            }
            else if (rack.state == COAP_STATE_ACTIVE)
            {
                leds_single_off(LEDS_RED);
                leds_single_on(LEDS_GREEN);
                res_oxygen.trigger();
            }
            else
            {
                LOG_ERR("ERROR: Invalid state");
            }
            etimer_reset(&rack.state_check_timer);  
        }
    }

    // clean data structure
    finish_rack();
    PROCESS_END();
}