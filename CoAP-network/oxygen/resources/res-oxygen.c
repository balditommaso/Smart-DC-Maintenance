#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <stdint.h>
#include "contiki.h"
#include "coap-engine.h"
#include "sys/node-id.h"
#include "../utils/coap-server-constants.h"
#include "../utils/json-message.h"
#include "../sensor-signs/sensor-sample.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "oxygen sensor"
#define LOG_LEVEL LOG_LEVEL_APP

struct oxygen_sensor {
    int o2_level;
    bool alarm;
};

// init sensor data
static struct oxygen_sensor o2_sensor = {.o2_level = OXYGEN_INIT, .alarm = false};

static void oxygen_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void oxygen_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void oxygen_event_handler(void);

EVENT_RESOURCE(res_oxygen,
                "title=\"oxygen\";rt=\"Text\" POST/PUT value=<value>;obs",
                oxygen_get_handler,
                NULL,
                oxygen_put_handler,
                NULL,
                oxygen_event_handler);

static void oxygen_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    char message[COAP_CHUNK_SIZE];
    set_json_msg_sample(message, COAP_CHUNK_SIZE, o2_sensor.o2_level);
    LOG_INFO("new sample: %s\n", message);
    size_t len = sizeof(message) - 1;

    memcpy(buffer, message, len);

    // send message
    coap_set_header_content_format(response, APPLICATION_JSON);
    coap_set_header_etag(response, (uint8_t*)&len, 1);
    coap_set_payload(response, buffer, len);
    coap_set_status_code(response, CONTENT_2_05);
}

static void oxygen_event_handler(void)
{
    int new_sample = get_oxygen_level(o2_sensor.o2_level, o2_sensor.alarm);
    if (new_sample != o2_sensor.o2_level)
    {
        o2_sensor.o2_level = new_sample;
        coap_notify_observers(&res_oxygen);
    }
}

static void oxygen_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    const char* text = NULL;
    size_t len = 0;
    bool success = true;
    char mode[4];
    memset(mode, 0, 3);

    LOG_INFO("Recived PUT request\n");
    len = coap_get_post_variable(request, "alarm", &text);
    if (len > 0 && len < 4)
    {
        memcpy(mode, text, len);
        if (strncmp(mode, "ON", len) == 0)
        {
            o2_sensor.alarm = true;
            LOG_INFO("Enable alarm");
        }
        else if (strncmp(mode, "OFF", len) == 0)
        {
            o2_sensor.alarm = false;
            LOG_INFO("Disabled alarm");
        }
        else
        {
            success = false;
        }
    }
    else
    {
        LOG_ERR("Not valid request.\n");
        success = false;
    }

    if (!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}