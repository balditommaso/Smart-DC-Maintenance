#include <stdio.h>
#include <string.h>
#include "os/sys/clock.h"
#include "json-message.h"
#include "../vital-signs/band-sample-constants.h"

static void clear_buffer(char *buffer, size_t size)
{
  memset(buffer, 0, size);
}

void set_json_msg_band_registration(char *message_buffer, size_t size, char *band_id)
{
  clear_buffer(message_buffer, size);
  snprintf(message_buffer, 
  				 size, 
  				 "{\"bandID\": \"%s\", \"registration\": true}",
  				 band_id);
}

void set_json_msg_status(char *message_buffer, size_t size, bool active)
{
  clear_buffer(message_buffer, size);
  snprintf(message_buffer,
  				 size, 
  				 "{\"status\": %d}", 
  				 active);
}

void set_json_msg_band_sample(char *message_buffer, size_t size, int battery_level, int oxygen_saturation, int blood_pressure, int temperature, int respiration, int heart_rate) 
{
  clear_buffer(message_buffer, size);
  snprintf(message_buffer,
  				 size, 
  				 "{\"batteryLevel\": %d, \"oxygenSaturation\": %d, \"bloodPressure\": \"%d\", \"temperature\": %d, \"respiration\": %d, \"heartRate\": \"%d\"}",
  				 battery_level,
  				 oxygen_saturation,
  				 blood_pressure,
  				 temperature,
  				 respiration,
  				 heart_rate);
}

void set_json_msg_alarm_started(char *message_buffer, size_t size)
{
  clear_buffer(message_buffer, size);
  snprintf(message_buffer, size, "%s", "{\"alarm\": true}");
}

