package net.sqdmc.factionshield;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class Shield implements ConfigurationSerializable{
	
	private final Set<ShieldBase> storage = new HashSet<ShieldBase>();
	public final ShieldOwner owner;
	public int ShieldPower;
	public int ShieldPowerMax;
	
	public Shield(ShieldOwner owner) {
		this.owner = owner;
	}
	
	public void addShield(ShieldBase shieldBase) {
		this.storage.add(shieldBase);
	}
	
	public ShieldOwner getOwner(){
		return owner;
	}
	
	public int getShieldPowerMax()
	{
		return ShieldPowerMax;
	}
	
	public void setMaxShieldPower(int shieldMaxPowerMax)
	{
		this.ShieldPowerMax = shieldMaxPowerMax;
	}
	
	public int getShieldPower()
	{
		return ShieldPower;
	}

	public void setShieldPower(int shieldpower)
	{
		this.ShieldPower = shieldpower;
	}
	
	@Override
	public String toString(){
		return "Owner:" + this.owner;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serial = new LinkedHashMap<String, Object>();
		
		serial.put("owner", this.owner.getId());
		
		List<Map<String, Object>> Owners = new ArrayList<Map<String,Object>>();
		Iterator<ShieldBase> i = storage.iterator();
		while(i.hasNext()) {
			ShieldBase s = i.next();
			Map<String, Object> shieldbases = new LinkedHashMap<String, Object>();

			// Mark this as a serialized ItemStack class so that Bukkit will
			// automatically deserialize it for us later
			shieldbases.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
					ShieldBase.class.getName());

			// Call the ItemStack's own serialize() and append the result to the list
			shieldbases.putAll(s.serialize());
			Owners.add(shieldbases);
		}
		// Add the list to the map
		serial.put("shieldbase", Owners);
		
		return serial;
	}
	
}
