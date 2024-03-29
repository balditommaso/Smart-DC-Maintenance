#include "sensor-sample.h"

int generate_sample(int last_sample, bool alarm, int lower_bound, int upper_bound, int good_value)
{
    int variation;
    int new_sample;
    if (alarm)
    {
        new_sample = last_sample + (int)(good_value - last_sample)*0.1;
    }
    else
    {
        if ((rand()%10) < 6) 
        {
            variation = (rand()%7)-2;
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
    }
    
    return new_sample;
}

int get_temperature(int last_sample, bool alarm)
{
    return generate_sample(last_sample, alarm, TEMPERATURE_LOWER_BOUND, TEMPERATURE_UPPER_BOUND, TEMPERATURE_GOOD_VALUE);
}

int get_humidity(int last_sample, bool alarm)
{
    return generate_sample(last_sample, alarm, HUMIDITY_LOWER_BOUND, HUMIDITY_UPPER_BOUND, HUMIDITY_GOOD_VALUE);
}

int get_oxygen_level(int last_sample, bool alarm)
{
    return generate_sample(last_sample, alarm, OXYGEN_LOWER_BOUND, OXYGEN_UPPER_BOUND, OXYGEN_GOOD_VALUE);
}