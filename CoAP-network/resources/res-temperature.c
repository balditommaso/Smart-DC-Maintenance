//#include <stdlib.h>
//#include <stdio.h>
//#include <string.h>
#include <time.h>
#include <stdint.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/node-id.h"
#include "./utils/coap-server-constants.h"
#include "./utils/json-message.h"
#include "../sensor-signs/sensor-sample.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "temperature-sensor"
#define LOG_LEVEL LOG_LEVEL_APP

struct temperature_sensor {
    int temp;
    bool alarm;
};

// init sensor data
static struct temperature_sensor temp_sensor = {.temp = TEMPERATURE_INIT, .alarm = false};

static void temperature_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temperature_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temperature_event_handler(void);

EVENT_RESOURCE(res_temperature,
                "title=\"temperature\" POST/PUT value=<value>;obs",
                temperature_get_handler,
                NULL,
                temperature_put_handler,
                NULL,
                temperature_event_handler);

static void temperature_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    char message[COAP_CHUNK_SIZE];
    set_json_msg_sample(message, COAP_CHUNK_SIZE, temp_sensor.temp);
    LOG_INFO("new sample: %s\n", message);
    size_t len = strlen(message);

    // send message
    coap_set_header_content_format(response, APPLICATION_JSON);
    coap_set_header_etag(response, (uint8_t*)&len, 1);
    coap_set_payload(response, message, len);
}

static void temperature_event_handler()
{
    int new_sample = get_temperature(temp_sensor.temp);
    LOG_INFO("new event triggered -> %d\n", new_sample);
    if (new_sample != temp_sensor.temp)
    {
        temp_sensor.temp = new_sample;
        coap_notify_observers(&res_temperature);
    }
}

static void temperature_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    const char* value = NULL;
    size_t len = 0;
    bool success = true;

    if ((len = coap_get_post_variable(request, "value", &value)))
    {
        LOG_INFO("new request: %s\n", value);
        if (parse_json_alarm(value, len))
            leds_single_on(LEDS_RED);
        else
            leds_single_off(LEDS_RED);
    }
    else
    {
        LOG_ERR("Not valid request.\n");
        success = false;
    }

    if (!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}