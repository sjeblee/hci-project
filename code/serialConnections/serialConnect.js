'use strict';

// Dependencies
var serialport = require('serialport');

// Port names - edit these fields as neccessary
var portNameArduino = '/dev/cu.usbmodem14141';
var portNameAndroid = '/dev/cu.usbmodem14122';

/*------------Arduino Serial Connection ---------------*/
var spArduino = new serialport(portNameArduino, {
    baudRate: 9600,
    dataBits: 8,
    parity: 'none',
    stopBits: 1,
    flowControl: false,
    parser: serialport.parsers.readline("\r\n")
});

// Read data from serial port and add to buff
spArduino.on('data', function(input) {
    console.log(input); 
	spAndroid.write(input);
}); 

/*------------- Android Serial Connection -------------*/
var spAndroid = new serialport(portNameAndroid, {
    baudRate: 9600, 
    dataBits: 8, 
    parity: 'none', 
    stopBits: 1, 
    flowControl: false, 
    parsers: serialport.parsers.readline("\r\n")
});

/*----------------- DEBUG CODE BELOW ------------------*/
// Comment out below when running prototype 
spAndroid.on('data', function(input) {
    console.log("Received on Android" + input); 
}); 

serialport.list(function (err, ports) {
    ports.forEach(function(port) {
        console.log(port.comName);
    });
});