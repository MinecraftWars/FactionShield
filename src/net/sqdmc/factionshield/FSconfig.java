package net.sqdmc.factionshield;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class FSconfig {
	private Logger log = Bukkit.getServer().getLogger();
	
	private FactionShield plugin;
	private static String pluginName;
	private static String pluginVersion;
	
	private static String directory = "plugins" + File.separator + FactionShield.getPluginName() + File.separator;
	private File configFile = new File(directory + "config.yml");
	private File durabilityFile = new File(directory + "durability.dat");
	private YamlConfiguration bukkitConfig = new YamlConfiguration();
	
	/**
	 * Default settings
	 */
	private int ProtectionRadius = 16;
	private long RegenTime = 60000L;
	private int PowerCost = 5;
	private int Durability = 20;
	
	public FSconfig(FactionShield plugin) {
		this.plugin = plugin;
		pluginName = FactionShield.getPluginName();
	}
	
	public boolean loadConfig() {
		boolean isErrorFree = true;
		pluginVersion = FactionShield.getVersion();

		new File(directory).mkdir();

		if (configFile.exists()) {
			try {
				bukkitConfig.load(configFile);

				if (bukkitConfig.getString("Version", "").equals(pluginVersion)) {
					// config file exists and is up to date
					log.info(pluginName + " config file found, loading config...");
					loadData();
				} else {
					// config file exists but is outdated
					log.info(pluginName + " config file outdated, adding old data and creating new values. " + "Make sure you change those!");
					loadData();
					writeDefault();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// config file does not exist
			try {
				log.info(pluginName + " config file not found, creating new config file...");
				configFile.createNewFile();
				writeDefault();
			} catch (IOException ioe) {
				log.severe("Could not create the config file for " + pluginName + "!");
				ioe.printStackTrace();
				isErrorFree = false;
			}
		}

		return isErrorFree;
	}
	
	private void loadData() {
		try {
			bukkitConfig.load(configFile);
			
			ProtectionRadius = bukkitConfig.getInt("ProtectionRadius.Distance", 16);
			RegenTime = readLong("RegenTime.Time", "600000");
			PowerCost = bukkitConfig.getInt("PowerCost.Amount", 5);
			Durability = bukkitConfig.getInt("Durability.Amount", 20);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeDefault() {
		write("Version", FactionShield.getVersion());
		
		write("ProtectionRadius.Distance", ProtectionRadius);
		write("RegenTime.Time", RegenTime);
		write("PowerCost.Amount", PowerCost);
		write("Durability.Amount", Durability);

		loadData();
	}
	
	private void write(String key, Object o) {
		try {
			bukkitConfig.load(configFile);
			bukkitConfig.set(key, o);
			bukkitConfig.save(configFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private long readLong(String key, String def) {
		try {
			bukkitConfig.load(configFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Bukkit Config has no getLong(..)-method, so we are using Strings
		String value = bukkitConfig.getString(key, def);

		long tmp = 0;

		try {
			tmp = Long.parseLong(value);
		} catch (NumberFormatException nfe) {
			log.warning("Error parsing a long from the config file. Key=" + key);
			nfe.printStackTrace();
		}

		return tmp;
	}
	
	public int getProtRadius(){
		return ProtectionRadius;
	}
	
	public long getRegenTime(){
		return RegenTime;
	}
	
	public int getPowerCost(){
		return PowerCost;
	}
	
	public int getDurability(){
		return Durability;
	}
	
	public void saveDurabilityToFile() {
		if (plugin.getListener() == null || plugin.getListener().getShieldDurability() == null) {
			return;
		}

		HashMap<Integer, Integer> map = plugin.getListener().getShieldDurability();

		new File(directory).mkdir();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(durabilityFile));
			oos.writeObject(map);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			log.severe("Failed writing obsidian durability for " + FactionShield.getPluginName());
			e.printStackTrace();
		}
	}

	/**
	 * Loads the durability hash map from a file.
	 * 
	 * @return the durability hash map from a file
	 */
	@SuppressWarnings("unchecked")
	public HashMap<Integer, Integer> loadDurabilityFromFile() {
		if (!durabilityFile.exists() || plugin.getListener() == null || plugin.getListener().getShieldDurability() == null) {
			return null;
		}

		new File(directory).mkdir();

		HashMap<Integer, Integer> map = null;
		Object result = null;

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(durabilityFile));
			result = ois.readObject();
			map = (HashMap<Integer, Integer>) result;
			ois.close();
		} catch (IOException ioe) {
			log.severe("Failed reading obsidian durability for " + FactionShield.getPluginName());
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			log.severe("Obsidian durability file contains an unknown class, was it modified?");
			cnfe.printStackTrace();
		}

		return map;
	}
}
