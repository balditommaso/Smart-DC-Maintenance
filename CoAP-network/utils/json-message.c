#include <stdio.h>
#include <string.h>
#include "os/sys/clock.h"
#include "json-message.h"
#include "../sensor-signs/sensor-sample-constants.h"

void set_json_msg_sensor_registration(char *message_buffer, size_t size, char *sensor_id, char *sensor_type)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"sensorId\": \"%s\", \"sensorType\": \"%s\"}",
                sensor_id,
                sensor_type);
}

void set_json_msg_temperature_sample(char *message_buffer, size_t size, int temperature)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"temperature\": \"%d\"}",
                temperature);
}

void set_json_msg_humidity_sample(char *message_buffer, size_t size, int humidity)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"humidity\": \"%d\"}",
                humidity);
}

void set_json_msg_oxygen_sample(char *message_buffer, size_t size, float oxygen_level)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"oxygenLevel\": \"%f\"}",
                oxygen_level);
}

void set_json_msg_check_request(char *message_buffer, size_t size)
{
    memset(message_buffer, 0, size);
    snprintf(message_buffer,
                size,
                "{\"checkRequest\": \"true\"}");
}

bool parse_json_alarm(const char *message_buffer, size_t size, int value)
{
    int len = sscanf(message_buffer, "{\"alarm\": \"%d\"}", &value);
    return (len == 1) ? true : false;
}