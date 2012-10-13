package net.sqdmc.factionshield;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

public class FSconfig {
	private Logger log = Bukkit.getServer().getLogger();
	
	private FactionShield plugin;
	private static String pluginName;
	private static String pluginVersion;
	
	private static String directory = "plugins" + File.separator + FactionShield.getPluginName() + File.separator;
	private File configFile = new File(directory + "config.yml");
	private File durabilityFile = new File(directory + "durability.dat");
	private File shieldsFile = new File(directory + "shields.yml");
	private File shieldbaseFile = new File(directory + "shieldbase.yml");
	private YamlConfiguration bukkitConfig = new YamlConfiguration();
	private YamlConfiguration shieldsDB = new YamlConfiguration();
	private YamlConfiguration shieldsBaseDB = new YamlConfiguration();
	
	private FileConfiguration shieldStorageConfig = null;
	private File shieldStorageFile = null;
	
	private ShieldStorage storage = null;
	
	/**
	 * Default settings
	 */
	private int ProtectionRadius = 16;
	private long RegenTime = 60000L;
	private int Durability = 100;
	
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
			Durability = bukkitConfig.getInt("Durability.Amount", 20);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeDefault() {
		write("Version", FactionShield.getVersion());
		
		write("ProtectionRadius.Distance", ProtectionRadius);
		write("RegenTime.Time", RegenTime);
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
	
	public int getMaxPowerCost(){
		return Durability;
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
			log.severe("Failed writing shields durability for " + FactionShield.getPluginName());
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
			log.severe("Failed reading shields durability for " + FactionShield.getPluginName());
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			log.severe("Shields durability file contains an unknown class, was it modified?");
			cnfe.printStackTrace();
		}

		return map;
	}
	
	private void write2(String key, Object o, File file) {
		try {
			shieldsDB.load(file);
			shieldsDB.set(key, o);
			shieldsDB.save(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void write3(String key, Object o, File file) {
		try {
			shieldsBaseDB.load(file);
			shieldsBaseDB.set(key, o);
			shieldsBaseDB.save(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void deserialize() {
		if(shieldStorageConfig == null) loadshieldStorage();
		
		//log.info(shieldStorageConfig.toString());
		
		log.info("[FactionShield] FactionShield: Storage config loaded successfully.");

		try {
			// Load our Storage class out of the config object
			storage = (ShieldStorage)shieldStorageConfig.get("storage");
		} catch (ClassCastException e) {
			// Config didn't give us the correct class
			log.info("[FactionShield] ShieldConfigr: Got an invalid class, expecting " + ShieldStorage.class.getName() + e);
		}
		if(storage == null) {
			// If for some reason our storage class failed to deserialize,
			// create a new blank one.
			storage = new ShieldStorage();
			log.warning("[FactionShield] ShieldConfigr: Failed to load from shields.yml, initialized new ShieldStorage!");	
		}

		else log.info("[FactionShield] ShieldConfigr: Successfully restored data.");

	}
	
	
	public void serialize() {
		if(shieldStorageConfig == null) loadshieldStorage();
		shieldStorageConfig.set("storage", storage);
		saveShieldStorage();
		log.info("[FactionShield] FactionShield: Successfully saved data.");
	}
	
	private void loadshieldStorage() {
		log.info("TEST");
		if(shieldStorageFile == null) {
			try{
			log.info("Creating Shields File... " + directory);
			shieldStorageFile = new File(directory, "shields.yml");
			shieldStorageFile.createNewFile();
			} catch (Exception e) {
				log.info("Error: " + e.toString());
			}
			if(shieldStorageFile.exists()) {
				try {
					shieldStorageConfig = YamlConfiguration.loadConfiguration(shieldStorageFile);
				} catch (Exception e) {
					log.info("Internal error occurred while loading storage.yml, falling back to defaults" + e.toString());
				    // Get default persistence file from the jar.
				    InputStream defaultStorageStream = plugin.getResource("/shields.yml");
				    if (defaultStorageStream != null) {
				    	// Set default values (e.g. empty storage) if no storage exists yet.
				    	shieldStorageConfig = YamlConfiguration.loadConfiguration(defaultStorageStream);
				    } else {
				    	shieldStorageConfig = new YamlConfiguration();
				    }
				}
			}
			else {
				log.info("[FactionShield] No storage file exists, creating a new one.");
				// Get default persistence file from the jar.
			    InputStream defaultStorageStream = plugin.getResource("/shields.yml");
			    if (defaultStorageStream != null) {
			    	// Set default values (e.g. empty storage) if no storage exists yet.
			    	shieldStorageConfig = YamlConfiguration.loadConfiguration(defaultStorageStream);
			    } else {
			    	shieldStorageConfig = new YamlConfiguration();
			    }
			}
		}
	}
	
	private void saveShieldStorage() {
		if (shieldStorageConfig == null || shieldStorageConfig == null) {
			// Don't save if there is nothing to save
		    return;
	    }

		// Make a backup of the old storage file just in case
		File backupFile = new File(directory, "shields.yml.old");
		try {
			// Create backup file if it doesn't exist (may throw IOException)
			backupFile.createNewFile();

			// Open files for reading/writing (may throw FileNotFoundException)
			FileInputStream in = new FileInputStream(shieldStorageFile);
			FileOutputStream out = new FileOutputStream(backupFile);

			// Set up buffer
			byte[] data = new byte[4096];
			int bytes = 0;

			// Copy data into backup file (may throw IOException)
			while((bytes = in.read(data)) >= 0) {
				out.write(data, 0, bytes);
			}

			// Close files (may throw IOException)
			out.close();
			in.close();
		} catch(java.io.FileNotFoundException ex) {
			log.warning("[FactionShield] FactionShield: Failed to back up shields.yml: File not found");
		} catch (IOException e) {
			log.warning("[FactionShield] FactionShield: Failed to back up shields.yml: Read or write error");
		}

	    try {
	    	// Attempt to write changed config to disk
	        shieldStorageConfig.save(shieldStorageFile);
	    } catch (IOException ex) {
	        log.info("Could not persist storage to " + shieldStorageFile + ex);
	    }
	}
	
	
	
	
	// Saves to file
	// This isn't working yet
	public void saveShieldsToFile() {
		if (plugin.getListener() == null || plugin.getListener().getShields() == null) {
			return;
		}
		
		if (!shieldsFile.exists())
		{
			try {
				shieldsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		
		try {
			shieldsDB.save(shieldsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap<ShieldOwner, Shield> map = plugin.getListener().getShields();

	    final Iterator<Entry<ShieldOwner, Shield>> iter = map.entrySet().iterator();


	    while (iter.hasNext()) {
	        final Entry<ShieldOwner, Shield> entry = iter.next();
	        final Object value = entry.getValue().owner.getId();
	        final String key = entry.getKey().toString();
	        write2(key, value, shieldsFile);
	    }
	    
	}
	
	// Load from file
	// things go horribly wrong...
	@SuppressWarnings("unchecked")
	public HashMap<ShieldOwner, Shield> loadShieldsFromFile() {
		if (!shieldsFile.exists() || plugin.getListener() == null || plugin.getListener().getShields() == null) {
			return null;
		}
		
		//if (directory.)
		new File(directory).mkdir();

		HashMap<ShieldOwner, Shield> map = new HashMap<ShieldOwner, Shield>();
		Object result = null;
		
		try {
			shieldsDB.load(shieldsFile);
			
			if (shieldsDB.contains("shieldOwner"))
			{
			String result1 = shieldsDB.getString("shieldOwner", "0");
			
			//Faction result2 = (Faction) shieldsDB.getList("net.sqdmc.factionshield." + result1 + ".owner");
			log.info(result1);
			
			
			Factions factions = Factions.i;
			
			if (!factions.exists(result1)){
				log.info("Faction doesn't exisit!");
				return null;
			}
			
			Faction faction = factions.get(result1);
			
			FactionShieldOwner fShieldOwner = new FactionShieldOwner(faction);
			//ShieldBase shieldbase = new ShieldBase();
			
			Shield shield = new Shield(fShieldOwner);
			
			shield.setShieldPower(100);
			shield.setMaxShieldPower(100);
			//ShieldBase shieldbase = new ShieldBase(null, null, shield, null, Durability, Durability, Durability);
		
			//log.info(fShieldOwner.getId());
			//log.info(fShieldOwner.toString());
			//log.info(shield.getOwner().toString());
		
			map.put(fShieldOwner, shield);
			
			return map;
			}
			//map = (HashMap<ShieldOwner, Shield>) result;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	
	
	public void saveShieldsBaseToFile() {
		if (plugin.getListener() == null || plugin.getListener().getShields() == null) {
			return;
		}
		
		if (!shieldbaseFile.exists())
		{
			try {
				shieldbaseFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		HashMap<Block, ShieldBase> map = plugin.getListener().getShieldsBase();
		Object obj = map;
		
		
		new File(directory).mkdir();

	    final Iterator<Entry<Block, ShieldBase>> iter = map.entrySet().iterator();


	    while (iter.hasNext()) {
	        final Entry<Block, ShieldBase> entry = iter.next();
	        final Object value = entry.getValue().getShieldBaseLoc();
	        final String key = entry.getValue().getShieldBase().toString();
	        write3(key, value, shieldbaseFile);
	    }
		
		
		
	}

	@SuppressWarnings("unchecked")
	public HashMap<Block, ShieldBase> loadShieldsBaseFromFile() {
		if (!shieldbaseFile.exists() || plugin.getListener() == null || plugin.getListener().getShieldsBase() == null) {
			return null;
		}

		new File(directory).mkdir();

		HashMap<Block, ShieldBase> map = null;
		ConfigurationSection shield = null;
		Map shieldvalues;
		String locationstring = null;
		int x;
		int y;
		int z;

		//shieldsBaseDB.
		shield = shieldsBaseDB.getConfigurationSection("Shield");
		shieldvalues = shield.getValues(true);
		
		
		
		//shieldvalues.get());
		
		
		log.info(shieldvalues.toString());
		//log.info(world);
		

		return map;	
	}
	
	
	
	
	
	
 	
	// Old method that attempted to save to .dat straight from the HashMaps
	// Doesn't work do to non-serialable objects.
	/*
	public void saveShieldsToFile() {
		if (plugin.getListener() == null || plugin.getListener().getShields() == null) {
			return;
		}

		HashMap<ShieldOwner, Shield> map = plugin.getListener().getShields();
		Object obj = map;

		new File(directory).mkdir();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(shieldsFile));
			oos.writeObject(obj);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			log.severe("Failed writing shields durability for " + FactionShield.getPluginName());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<ShieldOwner, Shield> loadShieldsFromFile() {
		if (!shieldsFile.exists() || plugin.getListener() == null || plugin.getListener().getShields() == null) {
			return null;
		}

		new File(directory).mkdir();

		HashMap<ShieldOwner, Shield> map = null;
		Object result = null;

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(shieldsFile));
			result = ois.readObject();
			map = (HashMap<ShieldOwner, Shield>) result;
			ois.close();
		} catch (IOException ioe) {
			log.severe("Failed reading shields for " + FactionShield.getPluginName());
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			log.severe("Shields file contains an unknown class, was it modified?");
			cnfe.printStackTrace();
		}

		return map;
	}
	
	
	
	
	public void saveShieldsBaseToFile() {
		if (plugin.getListener() == null || plugin.getListener().getShields() == null) {
			return;
		}

		HashMap<Block, ShieldBase> map = plugin.getListener().getShieldsBase();
		Object obj = map;
		
		new File(directory).mkdir();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(shieldbaseFile));
			oos.writeObject(obj);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			log.severe("Failed writing shields base for " + FactionShield.getPluginName());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<Block, ShieldBase> loadShieldsBaseFromFile() {
		if (!shieldbaseFile.exists() || plugin.getListener() == null || plugin.getListener().getShieldsBase() == null) {
			return null;
		}

		new File(directory).mkdir();

		HashMap<Block, ShieldBase> map = null;
		Object result = null;

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(shieldbaseFile));
			result = ois.readObject();
			map = (HashMap<Block, ShieldBase>) result;
			ois.close();
		} catch (IOException ioe) {
			log.severe("Failed reading shields base for " + FactionShield.getPluginName());
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			log.severe("Shields base file contains an unknown class, was it modified?");
			cnfe.printStackTrace();
		}

		return map;
	}*/
}
