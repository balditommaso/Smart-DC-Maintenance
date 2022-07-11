#include "sensor-sample.h"

int generate_sample(int last_sample, int lower_bound, int upper_bound)
{
    int variation;
    int new_sample;
    if ((rand()%10) < 6) 
    {
        variation = (rand()%5)-2;
        new_sample = last_sample + variation;

        if (new_sample > upper_bound)
            new_sample = upper_bound;
        
        if (new_sample < lower_bound)
            new_sample = lower_bound;
    }
    else
    {
        new_sample = last_sample;
    }
    return new_sample;
}

int get_temperature(int last_sample)
{
    return generate_sample(last_sample, TEMPERATURE_LOWER_BOUND, TEMPERATURE_UPPER_BOUND);
}

int get_humidity(int last_sample)
{
    return generate_sample(last_sample, HUMIDITY_LOWER_BOUND, HUMIDITY_UPPER_BOUND);
}

float get_oxygen_level(int last_sample)
{
    int variation = rand() % 16;
    float new_sample = last_sample + variation*0.75;
    if (new_sample > OXYGEN_UPPER_BOUND)
            new_sample = OXYGEN_UPPER_BOUND;
        
    if (new_sample < OXYGEN_LOWER_BOUND)
        new_sample = OXYGEN_LOWER_BOUND;

    return new_sample; 
}