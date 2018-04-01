package raspbootin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

public class IniFile {

	/**
	 * Hash mapa koja sadrzi kategorije (sekcije). Hash kljuc je naziv
	 * kategorije (string), a vrednost je hash mapa koja sadrzi parove
	 * (parametar, vrednost).
	 */
	private HashMap<String, HashMap<String, String>> categories = new HashMap<String, HashMap<String, String>>();

	private String fileName;

	/**
	 * Konstruise objekat sa datim parametrima.
	 * 
	 * @param filename
	 *            Naziv lokalnog INI fajla.
	 */
	public IniFile(String fileName) throws Exception {
		BufferedReader in = null;
		File f = new File(fileName);
		if (f.isDirectory()) {
			throw new Exception("Cannot open ini file: " + fileName + ", because there is a folder with the same name.");
		} else  if (!f.exists()) {
			f.createNewFile();
		}
		if (f.exists() && f.isFile()) {
			in = new BufferedReader(new FileReader(fileName));
			readIni(in);
			in.close();
			this.fileName = fileName;
		} 
	}

	private void readIni(BufferedReader in) {
		String line, key, value;
		String currentCategory = "default";
		HashMap<String, String> currentMap = new HashMap<String, String>();
		categories.put(currentCategory, currentMap);
		try {
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.equals("") || line.startsWith(";"))
					continue;
				if (line.charAt(0) == '[') {
					currentCategory = line.substring(1, line.length() - 1);
					currentMap = new HashMap<String, String>();
					categories.put(currentCategory, currentMap);
				} else {
					String[] keyValue = line.split("=");
					if (keyValue.length > 0) {
						key = keyValue[0].trim();
						value = keyValue[1].trim();
						currentMap.put(key, value);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Vraca vrednost datog parametra u obliku stringa.
	 * 
	 * @param category
	 *            Kategorija (sekcija) u kojoj se nalazi parametar
	 * @param key
	 *            Naziv parametra
	 * @return String koji sadrzi vrednost parametra
	 */
	public String getString(String category, String key, String defaultValue) {
		HashMap<String, String> hm = categories.get(category);
		if (hm == null) {
			HashMap<String, String> currentMap = new HashMap<String, String>();
			currentMap.put(key, defaultValue);
			categories.put(category, currentMap);
			return defaultValue;
		} else {
			String value = hm.get(key);
			if (value != null) {
				return value;
			} else {
				hm.put(key, defaultValue);
				return defaultValue;
			}
		}
	}

	/**
	 * Vraca vrednost datog parametra u obliku integera.
	 * 
	 * @param category
	 *            Kategorija (sekcija) u kojoj se nalazi parametar
	 * @param key
	 *            Naziv parametra
	 * @return Integer vrednost parametra
	 */
	public int getInt(String category, String key, int defaultValue) {
		HashMap<String, String> hm = categories.get(category);
		if (hm == null) {
			HashMap<String, String> currentMap = new HashMap<String, String>();
			currentMap.put(key, "" + defaultValue);
			categories.put(category, currentMap);
			return defaultValue;
		} else {
			String value = hm.get(key);
			if (value == null) {
				hm.put(key, "" + defaultValue);
				return defaultValue;
			}
			try {
				return (Integer.parseInt(value));
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				return defaultValue;
			}

		}
	}

	public void setString(String category, String key, String value) {
		HashMap<String, String> hm = categories.get(category);
		if (hm == null) {
			HashMap<String, String> currentMap = new HashMap<String, String>();
			currentMap.put(key, value);
			categories.put(category, currentMap);
		} else {
			hm.put(key, value);
		}
	}

	public void setInt(String category, String key, int value) {
		HashMap<String, String> hm = categories.get(category);
		if (hm == null) {
			HashMap<String, String> currentMap = new HashMap<String, String>();
			currentMap.put(key, "" + value);
			categories.put(category, currentMap);
		} else {
			hm.put(key, "" + value);
		}
	}

	public boolean removeCategory(String category) {
		HashMap<String, String> hm = categories.get(category);
		if (hm == null)
			return false;
		else {
			categories.remove(category);
			return true;
		}
	}

	public boolean removeItem(String category, String key) {
		HashMap<String, String> hm = categories.get(category);
		if (hm == null)
			return false;
		else {
			String value = hm.get(key);
			if (value != null) {
				hm.remove(key);
				return true;
			} else
				return false;
		}
	}

	public void saveIni() {
		if (this.fileName != null) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter(this.fileName + ".txt"));
				Set<String> cats = this.categories.keySet();
				for (String categoryName : cats) {
					if (categoryName.equals("default"))
						continue;
					out.println("[" + categoryName + "]");
					HashMap<String, String> category = this.categories.get(categoryName);
					if (category != null) {
						Set<String> cKeys = category.keySet();
						for (String itemName : cKeys) {
							String value = category.get(itemName);
							if (value != null)
								out.println(itemName + "=" + value);
						}
					}
				}
				out.close();
				File f = new File(this.fileName);
				f.delete();
				f = new File(this.fileName + ".txt");
				f.renameTo(new File(this.fileName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
