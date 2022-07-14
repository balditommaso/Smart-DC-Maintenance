#include <stdio.h>
#include <string.h>
#include "os/sys/clock.h"
#include "json-message.h"
#include "../sensor-signs/sensor-sample-constants.h"

void set_json_msg_sensor_registration(char *message_buffer, size_t size, char *sensor_id)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"rackSensorId\": \"%s\", \"alarm\": false}",
                sensor_id);
}

void set_json_msg_sample(char *message_buffer, size_t size, int value)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"value\": %d}",
                value);
}

/*
void set_json_msg_humidity_sample(char *message_buffer, size_t size, int humidity)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"humidity\": \"%d\"}",
                humidity);
}
*/

void set_json_msg_oxygen_sample(char *message_buffer, size_t size, float oxygen_level)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"oxygenLevel\": %4.2f}",
                oxygen_level);
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
