package net.sqdmc.factionshield;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;

import net.sqdmc.factionshield.ShieldOwner;

public class FactionShieldOwner extends ShieldOwner {

	@Override
	public Faction getFaction() {
		return shieldOwner.getFaction();
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return shieldOwner.getFaction().getId();
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

}
