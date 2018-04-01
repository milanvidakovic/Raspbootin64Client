package raspbootin.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jssc.SerialPort;
import jssc.SerialPortList;
import raspbootin.util.IniFile;

public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = -8308516394161893346L;

	private JPanel pnlSettings = new JPanel(); 
	private JLabel lComPort = new JLabel("Serial port:");
	private JComboBox<String> cbPorts = new JComboBox<String>();
	
	private JPanel pnlBottom = new JPanel(); 
	private JButton btnOk = new JButton("Ok");
	private JButton btnCancel = new JButton("Cancel");
	
	public SettingsDialog(MainFrame parent) {
		super(parent, true);
		setSize(400, 300);
		
		GridBagLayout gbl = new GridBagLayout();
		pnlSettings.setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		pnlSettings.add(lComPort, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		pnlSettings.add(cbPorts, gbc);
		getContentPane().add(pnlSettings, BorderLayout.CENTER);
		
		fillPortsCombo(parent.ini);
		
		pnlBottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
		pnlBottom.add(btnOk);
		pnlBottom.add(btnCancel);
		
		getContentPane().add(pnlBottom, BorderLayout.SOUTH);
		
		btnOk.addActionListener( e -> saveToIni(parent));
		btnCancel.addActionListener( e -> this.setVisible(false));
	}

	private void fillPortsCombo(IniFile ini) {
		String[] portNames = SerialPortList.getPortNames();
		for (String port : portNames) {
			cbPorts.addItem(port);
		}
		cbPorts.setSelectedItem(ini.getString("serial", "port", "COM5"));
	}

	private void saveToIni(MainFrame parent) {
		parent.ini.setString("serial", "port", cbPorts.getSelectedItem().toString());
		parent.serialPort = new SerialPort(cbPorts.getSelectedItem().toString());
		parent.ini.setInt("settings", "x", getLocation().x);
		parent.ini.setInt("settings", "y", getLocation().y);
		parent.ini.setInt("settings", "h", getSize().height);
		parent.ini.setInt("settings", "w", getSize().width);

		parent.ini.saveIni();
		setVisible(false);
	}

}
