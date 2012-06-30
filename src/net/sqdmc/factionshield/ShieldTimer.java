package net.sqdmc.factionshield;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public final class ShieldTimer extends TimerTask {
	
	private FactionShield plugin;
	private final Integer duraID;
	
	private Logger log = Bukkit.getServer().getLogger();
	
	public ShieldTimer(FactionShield plugin, Integer duraID) {
		this.plugin = plugin;
		this.duraID = duraID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		resetDurability(duraID);		
	}

	private void resetDurability(Integer id) {
		if (id == null) {
			return;
		}
		
		//log.info(plugin.getListener().toString());
		//log.info(plugin.getListener().getShieldDurability().toString());
		HashMap<Integer, Integer> map = plugin.getListener().getShieldDurability();
		
		if (map == null) {
			return;
		}
		
		//plugin.getListener().RegenPowerLoss();
		
		map.remove(id);
	}
}
