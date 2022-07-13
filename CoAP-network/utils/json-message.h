
void set_json_msg_sensor_registration(char *message_buffer, size_t size, char *sensor_id, char* sensor_type);

void set_json_msg_temperature_sample(char *message_buffer, size_t size, int temperature); 

void set_json_msg_humidity_sample(char *message_buffer, size_t size, int humidity); 

void set_json_msg_oxygen_sample(char *message_buffer, size_t size, float oxygen_level); 

void set_json_msg_check_request(char *message_buffer, size_t size);

bool parse_json_alarm(const char *message_buffer, size_t size, int value);