#include <ADCTouch.h>
 
int i = 0;       //reference values to remove offset
 
void setup()
{
    // No pins to setup, pins can still be used regularly, although it will affect readings
 
    Serial.begin(9600);
}
 
void loop()
{
    Serial.println(i);  
    i++; 
    delay(1000);
}
