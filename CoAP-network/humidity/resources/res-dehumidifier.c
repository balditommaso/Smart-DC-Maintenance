#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <stdint.h>
#include "contiki.h"
#include "coap-engine.h"
#include "os/dev/leds.h"
#include "sys/node-id.h"
#include "../utils/coap-server-constants.h"
#include "../utils/json-message.h"
#include "../../sensor-signs/sensor-sample.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "dehumidifier actuator"
#define LOG_LEVEL LOG_LEVEL_APP


static bool active = false;

static void ventilation_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_dehumidifier,
            "title=\"ventilation\";rt=\"Text\" POST/PUT value=<value>;obs",
                NULL,
                NULL,
                ventilation_put_handler,
                NULL);


static void ventilation_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
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
            leds_toggle(LEDS_RED);
            active = true;
            LOG_INFO("Dehumidifier ON\n");
        }
        else if (strncmp(mode, "OFF", len) == 0)
        {
            leds_on(LEDS_RED);
            active = false;
            LOG_INFO("Dehumidifier OFF\n");
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