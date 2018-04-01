# Raspbootin64Client

This is a Java client for the Raspbootin64 loader.
You can get the Raspbootin64 loader from the:
https://github.com/bztsrc/raspi3-tutorial/tree/master/14_raspbootin64

This is a loader for the bare metal programs compiled for the Raspberry Pi 3, 64-bit.

This Java program tries to connect to the serial port (given as the first command line parameter) and 
then tries to upload the given kernel8.img file (as the second command line parameter) to the Raspbootin64 loader. It looks like this:

java -jar Raspbootin64Client.jar COM3 C:\Temp\kernel8.img

Or, you can start this program like this:

java -jar Raspbootin64Client.jar gui

This will start the GUI and then you can do the same, but this time, you can use the built-in terminal to see what is RPI sending, and to type text to be sent via serial back to the RPI.

If the upload was successfull, the loader will start the uploaded program. 

Many thanks to Zoltan Baldaszti (https://github.com/bztsrc) who ported the Raspbootin to the 64-bit RPI3 version.

