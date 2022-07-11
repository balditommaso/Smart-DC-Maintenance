#include "band-sample.h"

static struct band_sample last_sample;

void init_sample_values()
{
	last_sample.oxygen_saturation = OXYGEN_SATURATION_INIT;
	last_sample.blood_pressure = BLOOD_PRESSURE_INIT;
	last_sample.temperature = TEMPERATURE_INIT;
	last_sample.respiration = RESPIRATION_INIT;
	last_sample.heart_rate = HEART_RATE_INIT;
}

double generate_sample(double last_sample, double max_deviation, double lower_bound, double upper_bound)
{
  double deviation, new_sample;

  double min = -1.0*max_deviation;
  double max = max_deviation;
  
  
  deviation = ((max-min)*rand())/RAND_MAX + min;
  new_sample = last_sample + deviation;

  if(new_sample < lower_bound) {
    return lower_bound;
  }

  if(new_sample > upper_bound) {
    return upper_bound;
  }

  return new_sample;
}

int get_oxygen_saturation()
{
	int oxygen_saturation_sample = generate_sample(last_sample.oxygen_saturation, 
												   OXYGEN_SATURATION_DEVIATION, 
												   OXYGEN_SATURATION_LOWER_BOUND, 
												   OXYGEN_SATURATION_UPPER_BOUND);
	last_sample.oxygen_saturation = oxygen_saturation_sample;
	return oxygen_saturation_sample;
}

int get_blood_pressure()
{
	int blood_pressure_sample = generate_sample(last_sample.blood_pressure,
												BLOOD_PRESSURE_DEVIATION,
												BLOOD_PRESSURE_LOWER_BOUND,
												BLOOD_PRESSURE_UPPER_BOUND);
	last_sample.blood_pressure = blood_pressure_sample;
	return blood_pressure_sample;
}

double get_temperature()
{
	double temperature_sample = generate_sample(last_sample.temperature,
												TEMPERATURE_DEVIATION,
												TEMPERATURE_LOWER_BOUND,
												TEMPERATURE_UPPER_BOUND);
	temperature_sample = roundf(temperature_sample * 10) / 10;	// 1 decimal place
	last_sample.temperature = temperature_sample;
	return temperature_sample;
}

int get_respiration()
{
	int respiration_sample = generate_sample(last_sample.respiration,
												RESPIRATION_DEVIATION,
												RESPIRATION_LOWER_BOUND,
												RESPIRATION_UPPER_BOUND);
	last_sample.respiration = respiration_sample;
	return respiration_sample;
}

int get_heart_rate()
{
	int heart_rate_sample = generate_sample(last_sample.heart_rate,
												HEART_RATE_DEVIATION,
												HEART_RATE_LOWER_BOUND,
												HEART_RATE_UPPER_BOUND);
	last_sample.heart_rate = heart_rate_sample;
	return heart_rate_sample;
}

