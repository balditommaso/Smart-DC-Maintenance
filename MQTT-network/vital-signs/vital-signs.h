#include <stdlib.h>
#include "vital-signs-constants.h"

struct vital_signs_sample {
	int oxygen_saturation;
	int blood_pressure;
	int	temperature;
	int respiration;
	int heart_rate;
};

int get_oxygen_saturation();

int get_blood_pressure();

int get_temperature();

int get_respiration();

int get_heart_rate();
