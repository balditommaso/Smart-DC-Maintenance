#include <stdlib.h>
#include <math.h>
#include "band-sample-constants.h"

struct band_sample {
	int oxygen_saturation;
	int blood_pressure;
	double	temperature;
	int respiration;
	int heart_rate;
};

void init_sample_values();

int get_oxygen_saturation();

int get_blood_pressure();

double get_temperature();

int get_respiration();

int get_heart_rate();
