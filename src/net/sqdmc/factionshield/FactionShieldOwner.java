package net.sqdmc.factionshield;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import net.sqdmc.factionshield.ShieldOwner;

public class FactionShieldOwner extends ShieldOwner implements ConfigurationSerializable {

	@Override
	public Faction getFaction() {
		return shieldOwner;
	}
	
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return shieldOwner.getId();
	}
	
	public void setFaction(String Id){
		Faction faction = Factions.i.get(Id);
		
		this.shieldOwner = faction;
	}

	@Override
	public void sendMessage(String message) {
		shieldOwner.sendMessage(message);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((shieldOwner == null) ? 0 : shieldOwner.getId().hashCode());
		return result;
	}

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FactionShieldOwner other = (FactionShieldOwner) obj;
        return shieldOwner.getId().equals(other.shieldOwner.getId());
    }

	public Faction shieldOwner;
	
	public FactionShieldOwner(Faction factionId) {
		this.shieldOwner = factionId;
	}
	
	@Override
	public String toString() {
		return "shieldOwner";
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serial = new LinkedHashMap<String, Object>();
		
		serial.put("shieldOwner", this.shieldOwner);
		
		return serial;
	}
}
