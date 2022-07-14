#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <stdint.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/node-id.h"
#include "../utils/coap-server-constants.h"
#include "../utils/json-message.h"
#include "../../sensor-signs/sensor-sample.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "temperature sensor"
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
                "title=\"temperature\";rt=\"Text\" POST/PUT value=<value>;obs",
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
    size_t len = sizeof(message) - 1;

    memcpy(buffer, message, len);

    // send message
    coap_set_header_content_format(response, APPLICATION_JSON);
    coap_set_header_etag(response, (uint8_t*)&len, 1);
    coap_set_payload(response, buffer, len);
    coap_set_status_code(response, CONTENT_2_05);
}

static void temperature_event_handler(void)
{
    int new_sample = get_temperature(temp_sensor.temp, temp_sensor.alarm);
    if (new_sample != temp_sensor.temp)
    {
        temp_sensor.temp = new_sample;
        coap_notify_observers(&res_temperature);
    }
}

static void temperature_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
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
            leds_single_on(LEDS_RED);
            temp_sensor.alarm = true;
            LOG_INFO("Enable alarm: %d\n", temp_sensor.temp);
        }
        else if (strncmp(mode, "OFF", len) == 0)
        {
            leds_single_off(LEDS_RED);
            temp_sensor.alarm = false;
            LOG_INFO("Disabled alarm: %d\n", temp_sensor.temp);
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