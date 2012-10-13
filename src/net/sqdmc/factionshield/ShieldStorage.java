package net.sqdmc.factionshield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class ShieldStorage implements ConfigurationSerializable {
	
	private String Id;
	private HashMap<ShieldOwner, Shield> shields; // = new HashMap<ShieldOwner, Shield>();
	private HashMap<Block, ShieldBase> blockShieldBase; // = new HashMap<Block, ShieldBase>();

	public ShieldStorage()
	{
		Id = "0";
		shields = new HashMap<ShieldOwner, Shield>();
		blockShieldBase = new HashMap<Block, ShieldBase>();
	}
	
	public ShieldStorage(Map<String, Object> serialData){
		
	}
	
	public HashMap<ShieldOwner, Shield> getShields()
	{
		return this.shields;
	}
	
	public void setShields(HashMap<ShieldOwner, Shield> map){
		this.shields = map;
	}
	
	public HashMap<Block, ShieldBase> getBlockShieldBase()
	{
		return this.blockShieldBase;
	}
	
	public void setBlockShieldBase(HashMap<Block, ShieldBase> map){
		this.blockShieldBase = map;
	}
	
	public void addBlockShieldBase(Block signBlock, ShieldBase shieldbase){
		blockShieldBase.put(signBlock, shieldbase);
	}
	
	public void addShield(ShieldOwner owner,Shield shield){
		shields.put(owner,shield);
	}
	
	public void removeBlockShieldBase(Block block){
		blockShieldBase.remove(block);
	}
	
	public void removeShields(ShieldOwner owner){
		shields.remove(owner);
	}
	
	@Override
	public Map<String, Object> serialize() {
		
		shields = this.getShields();

		Map<String, Object> serial = new LinkedHashMap<String, Object>();
		
		//serial.put("Owner", Id);
		
		List<Map<String, Object>> subsection = new ArrayList<Map<String, Object>>();
		for (Map.Entry<ShieldOwner, Shield> set : shields.entrySet())
		{
			//set.getKey();
			//set.getValue().owner.getId();
			serial.put("Shield", set.getValue().owner.getId());
		}
		
		/*List<Map<String, Object>> subsection = new ArrayList<Map<String, Object>>();
		{ // New scope for iterator
			Iterator<Shield> i = shields.values().iterator();
			while(i.hasNext()) {
				Shield t = i.next();
				Map<String, Object> shield = new LinkedHashMap<String, Object>();
				shield.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
						t.getClass().getName());
				shield.putAll(t.serialize());
				subsection.add(shield);
			}
		}
		serial.put("shields", subsection);
		
		subsection = new ArrayList<Map<String, Object>>();
		{ // New scope for iterator
			Iterator<ShieldBase> i = blockShieldBase.values().iterator();
			while(i.hasNext()) {
				ShieldBase t = i.next();
				Map<String, Object> shieldbase = new LinkedHashMap<String, Object>();
				shieldbase.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
						t.getClass().getName());
				shieldbase.putAll(t.serialize());
				subsection.add(shieldbase);
			}
		}
		// Add to map
		serial.put("shieldbase", subsection);*/
		
		
		return serial;
	}

}
