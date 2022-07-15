#include <stdio.h>
#include <string.h>
#include "json-message.h"
#include "../sensor-signs/sensor-sample-constants.h"

void set_json_msg_sensor_registration(char *message_buffer, size_t size, char* type)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"type\": \"%s\"}",
                type);
}

void set_json_msg_sample(char *message_buffer, size_t size, int value)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"value\": %d}",
                value);
}

void set_json_msg_check_request(char *message_buffer, size_t size)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"checkRequest\": \"true\"}");
}

int parse_json_alarm(const char *message_buffer, size_t size)
{
    int value;
    sscanf(message_buffer, "{\"alarm\": \"%d\"}", &value);
    return value;
}

int parse_json_registration(const char *message_buffer, size_t size)
{
    int value;
    sscanf(message_buffer, "{\"registration\": \"%d\"}", &value);
    return value;
}

void set_json_success_registration(char *message_buffer, size_t size)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"registration\": 1}");
}
