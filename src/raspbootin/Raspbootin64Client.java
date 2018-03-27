package raspbootin;

import java.io.File;
import java.io.FileInputStream;

import jssc.SerialPort;
import jssc.SerialPortException;

public class Raspbootin64Client {

	void connectAndSend(String portName, String fileName) throws Exception {
		SerialPort serialPort = new SerialPort(portName);
		try {
			serialPort.openPort();// Open serial port
			// Set params. Also you can set params by this string:
			// serialPort.setParams(9600, 8, 1, 0);
			serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			System.out.println("Connecting to the Raspbootin...");
			// Read 8+3 bytes from the serial port (name + load_ready)
			byte[] buffer;
			String s = "";
			do {
				buffer = serialPort.readBytes(1);
				if (buffer[0] != '\r' && buffer[0] != '\n' && buffer[0] != 3) {
					s += new String(buffer);
				}
			} while (buffer[0] != 3);
			buffer = new byte[2];
			// get two more 0x03 bytes
			buffer = serialPort.readBytes(2);
			System.out.println("Raspbootin ready: [" + s + "]");

			if (buffer[0] == 3 && buffer[1] == 3) {
				System.out.println("Ready to send kernel file " + fileName);
				// ready to send file
				File f = new File(fileName);

				if (f.exists() && f.isFile()) {

					long size = f.length();

					serialPort.writeByte((byte) (size & 255));
					size >>= 8;
					serialPort.writeByte((byte) (size & 255));
					size >>= 8;
					serialPort.writeByte((byte) (size & 255));
					size >>= 8;
					serialPort.writeByte((byte) (size & 255));

					// Read 2 bytes from the serial port (file size acknowledge or error)
					buffer = serialPort.readBytes(2);
					System.out.println("Got the response from Raspbootin64: " + new String(buffer));

					if (buffer[0] == 'O' && buffer[1] == 'K') {
						// File length OK, proceed with upload.
						FileInputStream in = new FileInputStream(f);
						buffer = new byte[1024];
						int read, total = 0;
						while ((read = in.read(buffer)) != -1) {
							for (int i = 0; i < read; i++) {
								serialPort.writeByte(buffer[i]);
							}
							total += read;
						}
						in.close();
						System.out.println(
								"Sent " + total + " bytes, which must be the same as the file size: " + f.length());
					} else {
						System.err.println("File size error from server: [" + new String(buffer) + "]");
					}
				} else {
					System.err.println("File " + fileName + " does not exist, or is a folder!");
				}
			} else {
				System.err.println("Got wrong bytes from Raspbootin64: [" + new String(buffer) + "]");
			}

			serialPort.closePort();// Close serial port
		} catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}

	public static void main(String[] args) {
		try {
			if (args.length == 2)
				(new Raspbootin64Client()).connectAndSend(args[0], args[1]);
			else {
				System.err.println("Usage: java Raspbootin64Client <com_port> <file_path>");
				System.err.println("Example: java Raspbootin64Client COM3 C:\\Temp\\kernel8.img");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
