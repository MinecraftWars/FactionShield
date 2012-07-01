package net.sqdmc.factionshield;

import java.util.logging.Logger;


import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FactionShield extends JavaPlugin {
	PluginManager pluginmanager;
	Logger log = Bukkit.getServer().getLogger();
	private static final String PLUGIN_NAME = "FactionShield";
	private static String version;
	private FSconfig config = new FSconfig(this);
	private final ShieldListener entityListener = new ShieldListener(this);
		
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		version = pdfFile.getVersion();

		log.info(PLUGIN_NAME + " v" + version + " enabled");
		
		pluginmanager = getServer().getPluginManager();
		
		config.loadConfig();
		entityListener.setShieldDurability(config.loadDurabilityFromFile());
		//entityListener.setShields(config.loadShieldsFromFile());
		//entityListener.setShieldBase(config.loadShieldsBaseFromFile());
		
		getServer().getPluginManager().registerEvents(entityListener, this);
	}
	
	public void onDisable(){
		config.saveDurabilityToFile();
		config.saveShieldsToFile();
		//config.saveShieldsBaseToFile();
		log.info(PLUGIN_NAME + " disabled");
	}
	
	public FSconfig getFSconfig() {
		return config;
	}
    
	/*private void registerEvents() {
		registerEvent(new ShieldListener());
		//registerEvent(new ShieldBase());
	}
	
	public void registerEvent(Listener listener) {
		pluginmanager.registerEvents(listener, this);
	}*/
	
	public ShieldListener getListener() {
		return entityListener;
	}
	
	public static String getPluginName() {
		return PLUGIN_NAME;
	}
	
	public static String getVersion() {
		return version;
	}
}
