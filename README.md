# hci-project

Serena Jeblee sjeblee@cs.toronto.edu
Gurleen Kaur gurleen.kaur@mail.utoronto.ca
Shamama Khattak shamama.khattak@mail.utoronto.ca
Dina Sabie dina.sabie@mail.utoronto.ca

Watch app instructions
- If the watch isn't recognized,
edit this file: /etc/udev/rules.d/51-android.rules
and add the following line:

SUBSYSTEM=="usb", ATTRS{idVendor}=="0fce", ATTRS{idProduct}=="a1bd", MODE="0666"

then run:
sudo udevadm control --reload-rules
sudo service udev restart