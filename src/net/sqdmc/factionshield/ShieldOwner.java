package net.sqdmc.factionshield;

import com.massivecraft.factions.Faction;

abstract class ShieldOwner {
	
	abstract public Faction getFaction();
	abstract public String getName();
		
	/** Send message to the account holder. */
	abstract public void sendMessage(String message);

	abstract public int hashCode();
	abstract public boolean equals(Object other);
}
