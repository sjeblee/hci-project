#include <ADCTouch.h>
 
int ref0, ref1, ref2, ref3, ref4, ref5;       //reference values to remove offset
 
void setup()
{
    // No pins to setup, pins can still be used regularly, although it will affect readings
 
    Serial.begin(9600);
 
    ref0 = ADCTouch.read(A0, 500);    //create reference values to
    ref1 = ADCTouch.read(A1, 500);      //account for the capacitance of the pad
    ref2 = ADCTouch.read(A2, 500);    
    ref3 = ADCTouch.read(A3, 500);
    ref4 = ADCTouch.read(A4, 500);    
    ref5 = ADCTouch.read(A5, 500);
}
 
void loop()
{
    String iter; 
    
    int value0 = ADCTouch.read(A0);   
    int value1 = ADCTouch.read(A1);     
    int value2 = ADCTouch.read(A2);   
    int value3 = ADCTouch.read(A3);
    int value4 = ADCTouch.read(A4);   
    int value5 = ADCTouch.read(A5);
 
    value0 -= ref0;       //remove offset
    value1 -= ref1;       //remove offset
    value2 -= ref2;       //remove offset
    value3 -= ref3;       //remove offset
    value4 -= ref4;       //remove offset
    value5 -= ref5;

    iter = "" + (String)(value5) + " ";
    iter = iter + (String)(value4) + " ";
    iter = iter + (String)(value3) + " ";
    iter = iter + (String)(value2) + " ";
    iter = iter + (String)(value1) + " ";
    iter = iter + (String)(value0);
    
    Serial.println(iter);
    delay(100);
}
