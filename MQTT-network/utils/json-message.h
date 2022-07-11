
#ifndef SDCM_JSON_MESSAGE_H
#define SDCM_JSON_MESSAGE_H

void set_json_msg_band_registration(char *message_buffer, size_t size, char *band_id);

void set_json_msg_status(char *message_buffer, size_t size, bool active);

void set_json_msg_band_sample(char *message_buffer, size_t size, int battery_level, int oxygen_saturation, int blood_pressure, double temperature, int respiration, int heart_rate); 

void set_json_msg_alert_stopped(char *message_buffer, size_t size);

#endif /* SDCM_JSON_MESSAGE_H */
