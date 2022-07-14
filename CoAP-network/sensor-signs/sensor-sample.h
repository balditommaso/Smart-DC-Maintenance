#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include "sensor-sample-constants.h"

int get_temperature(int last_sample, bool alarm);

int get_humidity(int last_sample, bool alarm);

int get_oxygen_level(int last_sample, bool alarm);