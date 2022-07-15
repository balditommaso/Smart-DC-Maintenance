
void set_json_msg_sensor_registration(char *message_buffer, size_t size, char *type);

void set_json_msg_sample(char *message_buffer, size_t size, int value);

void set_json_msg_check_request(char *message_buffer, size_t size);

int parse_json_alarm(const char *message_buffer, size_t size);

int parse_json_registration(const char *message_buffer, size_t size);