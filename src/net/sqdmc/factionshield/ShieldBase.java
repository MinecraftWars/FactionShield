package net.sqdmc.factionshield;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class ShieldBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final Block sponge;
	public final Block sign;
	public final Shield shield;
	public final World world;
	public final int x;
	public final int y;
	public final int z;  
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//result = prime * result + ((sponge == null) ? 0 : sponge.getbgetBlock().hashCode());
		result = prime * result + ((sign == null) ? 0 : sign.getLocation().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		// FIXME probably need to manually implement based on block locations
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShieldBase other = (ShieldBase) obj;
		if (shield == null) {
			if (other.shield != null)
				return false;
		} else if (!shield.equals(other.shield))
			return false;
		if (sign == null) {
			if (other.sign != null)
				return false;
		} else if (!sign.equals(other.sign))
			return false;
		return true;
	}
	
	public ShieldBase(Block sponge, Block sign, Shield owner, World world, int x, int y, int z) {
		this.sponge = sponge;
		this.sign = sign;
		this.shield = owner;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		shield.addShield(this);
	}
	
	public String getShieldBaseLoc() {
		return world.getName() + "," + x  + "," + y + "," + z;
	}

	public void destroy() {
		ShieldStorage shieldstorage = new ShieldStorage();
		shieldstorage.removeShields(this.shield.getOwner());
		shieldstorage.removeBlockShieldBase(this.sponge);
		shieldstorage.removeBlockShieldBase(this.sign);
	}
	
	public String getShieldBase(){
		//return "Shield" + ".Location.World." + world.getName() + ".X=" + x + ".Y=" + y + ".Z=" + z;
		//return "Shield" + ".Location.World." + world.getName();
		return "Shield." + this.shield.owner.getId();
	}
	
	@Override
	public String toString(){
		return + x  + "," + y + "," + z;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> serial = new LinkedHashMap<String, Object>();

		serial.put("shield", shield.getOwner().getId());
		
		serial.put("shieldlocation", world.getName() + "," + x  + "," + y + "," + z);
		return serial;
	}
}
