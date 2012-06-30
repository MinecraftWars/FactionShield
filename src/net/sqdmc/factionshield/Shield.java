package net.sqdmc.factionshield;

import java.util.HashSet;
import java.util.Set;

public class Shield {
	
	private final Set<ShieldBase> storage = new HashSet<ShieldBase>();
	public final ShieldOwner owner;
	
	public Shield(ShieldOwner owner) {
		this.owner = owner;
	}
	
	public void addShield(ShieldBase shieldBase) {
		this.storage.add(shieldBase);
	}
	
	public ShieldOwner getOwner(){
		return owner;
	}
}
