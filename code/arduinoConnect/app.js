'use strict';

// Dependencies
var express = require('express');
var bodyParser = require('body-parser');
var app = express();

// Set views path
app.use(express.static(__dirname + '/'));

// Middleware
app.use(bodyParser.json());       
app.use(bodyParser.urlencoded({ // to support URL-encoded bodies
    extended: true
}));


// Initialize serial port to read from 
var serialport = require('serialport');
var portName = '/dev/ttyACM0';

var sp = new serialport(portName, {
    baudRate: 9600,
    dataBits: 8,
    parity: 'none',
    stopBits: 1,
    flowControl: false,
    parser: serialport.parsers.readline("\r\n")
});

// Globals 
var buf = []

// Read data from serial port and add to buff
sp.on('data', function(input) {
    buf.push(input)
    if (buf.length > 150) {
	buf.shift()
    }
});

// Get navigation bar 
app.get('/touchInfo', function(req, res) {
	console.log(buf);
    res.send(buf);
    buf = [];
});  

// Start the server and socket 
var server = app.listen(3000);
console.log("listening on port 3000")
