package raspbootin.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import raspbootin.Raspbootin64Client;
import raspbootin.util.IniFile;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -2333246467400263743L;
	public static byte[] readBytes = null;
	IniFile ini;

	private JPanel pnlCenter = new JPanel();
	private JLabel lImgFile = new JLabel("Image file: ");
	private JTextField tfImgFile = new JTextField();
	private JButton btnBrowse = new JButton("Browse");
	private JButton btnLoad = new JButton("Load&Execute");
	private JButton btnOpenSerial = new JButton("Open terminal");
	private JTextArea taIo = new JTextArea();
	private JScrollPane sc = new JScrollPane(taIo);

	private JPanel pnlBottom = new JPanel();
	private JButton btnSettings = new JButton("Settings");
	private JButton btnExit = new JButton("Exit");

	private SettingsDialog settings;

	SerialPort serialPort;

	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			this.ini = new IniFile("raspbootin.ini");
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Problem with opening ini file: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		setLocation(ini.getInt("main", "x", 100), ini.getInt("main", "y", 100));
		setSize(ini.getInt("main", "w", 800), ini.getInt("main", "h", 600));

		serialPort = new SerialPort(ini.getString("serial", "port", "COM5"));

		settings = new SettingsDialog(this);
		settings.setLocation(ini.getInt("settings", "x", 100), ini.getInt("settings", "y", 100));
		settings.setSize(ini.getInt("settings", "w", 800), ini.getInt("settings", "h", 600));
				
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closed");
				ini.setInt("main", "x", getLocation().x);
				ini.setInt("main", "y", getLocation().y);
				ini.setInt("main", "h", getSize().height);
				ini.setInt("main", "w", getSize().width);

				ini.setInt("settings", "x", settings.getLocation().x);
				ini.setInt("settings", "y", settings.getLocation().y);
				ini.setInt("settings", "h", settings.getSize().height);
				ini.setInt("settings", "w", settings.getSize().width);

				ini.saveIni();

				try {
					if (serialPort.isOpened()) {
						serialPort.closePort();
					}
				} catch (SerialPortException e1) {
					e1.printStackTrace();
				}
			}
		});

		setUpLayout();
		setVisible(true);
	}

	private void setUpLayout() {
		GridBagLayout gbl = new GridBagLayout();
		pnlCenter.setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		pnlCenter.add(lImgFile, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		pnlCenter.add(tfImgFile, gbc);
		tfImgFile.setText(ini.getString("image", "fileName", ""));

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		pnlCenter.add(btnBrowse, gbc);
		btnBrowse.addActionListener(e -> browseForImgFile());

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 0;
		pnlCenter.add(btnLoad, gbc);
		btnLoad.addActionListener(e -> loadImage());

		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 0;
		pnlCenter.add(btnOpenSerial, gbc);
		btnOpenSerial.addActionListener(e -> openSerialTerminal());

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 5;
		gbc.weighty = 1;
		pnlCenter.add(sc, gbc);
		taIo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				e.consume();
				if (e.getKeyChar() == '\n') {
					return;
				}
				try {
					if (serialPort != null && serialPort.isOpened()) {
						serialPort.writeByte((byte) e.getKeyChar());
					}
				} catch (SerialPortException e1) {
					e1.printStackTrace();
				}
			}
		});

		getContentPane().add(pnlCenter, BorderLayout.CENTER);

		pnlBottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
		pnlBottom.add(btnSettings);
		pnlBottom.add(btnExit);
		btnSettings.addActionListener(e -> settings.setVisible(true));
		btnExit.addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

		getContentPane().add(pnlBottom, BorderLayout.SOUTH);
	}

	private void initSerialCommunication() throws SerialPortException {
		if (!this.serialPort.isOpened()) {
			this.serialPort.openPort();
			this.serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		}
		serialPort.addEventListener(new SerialPortEventListener() {
			@Override
			public void serialEvent(SerialPortEvent e) {
				if (e.isRXCHAR() && e.getEventValue() > 0) {
					try {
						String receivedData = serialPort.readString(e.getEventValue());
						taIo.append(receivedData);
						taIo.setCaretPosition(taIo.getText().length());
					} catch (SerialPortException ex) {
						ex.printStackTrace();
					}
				}
			}
		}, SerialPort.MASK_RXCHAR);
	}

	private void openSerialTerminal() {
		if (!running) {
			running = true;
			btnOpenSerial.setText("Close terminal");
			btnLoad.setEnabled(false);
			taIo.requestFocus();
			try {
				if (!this.serialPort.isOpened()) {
					initSerialCommunication();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			running = false;
			btnOpenSerial.setText("Open terminal");
			btnLoad.setEnabled(true);
			try {
				if (serialPort.isOpened())
					serialPort.closePort();
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
		}
	}

	boolean running = false;
	SwingWorker<String, String> worker;

	private void loadImage() {
		if (!running) {
			running = true;
			btnLoad.setText("Stop");
			btnOpenSerial.setEnabled(false);
			taIo.requestFocus();
			worker = new SwingWorker<String, String>() {
				@Override
				protected String doInBackground() throws Exception {
					try {
						File imgFile = new File(tfImgFile.getText());
						if (imgFile.exists() && imgFile.isFile()) {
							publish("\nPower on your RPI3...\n");
							Raspbootin64Client.connectAndSend(MainFrame.this.serialPort, tfImgFile.getText(),
									toPrint -> publish(toPrint));
							initSerialCommunication();
						} else {
							JOptionPane.showMessageDialog(MainFrame.this,
									imgFile.getCanonicalPath() + " does not exist, or is a folder!", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return "Done";
				}

				@Override
				protected void process(java.util.List<String> chunks) {
					taIo.append(chunks.get(0));
					taIo.setCaretPosition(taIo.getText().length());
				}

				/**
				 * Poziva se kada se zavrsi izvrsenje (milom ili silom).
				 */
				@Override
				protected void done() {
				}
			};
			worker.execute();
		} else {
			synchronized(MainFrame.this) {
				MainFrame.this.notify();
			}
			worker.cancel(true);
			running = false;
			btnLoad.setText("Load&Execute");
			btnOpenSerial.setEnabled(true);
			try {
				if (serialPort.isOpened())
					serialPort.closePort();
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
		}

	}

	private void browseForImgFile() {
		try {
			JFileChooser fc = new JFileChooser(ini.getString("image", "fileName", "."));
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File chosen = fc.getSelectedFile();
				tfImgFile.setText(chosen.getCanonicalPath());
				ini.setString("image", "fileName", chosen.getCanonicalPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Problem with image file");
		}
	}

	public static void main(String[] args) {
		new MainFrame();
	}
}
