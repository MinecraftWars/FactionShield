package net.sqdmc.factionshield;

import java.io.Serializable;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class ShieldBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final Block sponge;
	public final Sign sign;
	public final Shield shield;
	public final org.bukkit.World world;
	public final int x;
	public final int y;
	public final int z;  
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//result = prime * result + ((sponge == null) ? 0 : sponge.getbgetBlock().hashCode());
		result = prime * result + ((sign == null) ? 0 : sign.getBlock().getLocation().hashCode());
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
	
	public ShieldBase(Block sponge, Sign sign, Shield owner, org.bukkit.World world, int x, int y, int z) {
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
		return world + "," + x  + "," + y + "," + z;
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}
}
