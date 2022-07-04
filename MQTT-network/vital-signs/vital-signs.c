#include "vital-signs.h"

static struct vital_signs_sample last_sample;

int generate_sample(int last_sample, int max_deviation, int lower_bound, int upper_bound)
{
  int deviation, new_sample;

  int min = -1*max_deviation;
  int max = max_deviation;
  
  
  deviation = min + rand()/(RAND_MAX/(max - min +  1) + 1);
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

