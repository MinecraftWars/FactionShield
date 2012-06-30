package net.sqdmc.factionshield;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;

import net.sqdmc.factionshield.FactionShield;
//import 

public class Commands implements CommandExecutor  {
	Logger log = Bukkit.getServer().getLogger();
	 
	private FactionShield plugin;
	 
	public Commands(FactionShield plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		log.info("Command Received: " + cmd);

		Faction faction = new Faction();
		Player player;
		if (sender instanceof Player) {
			player = (Player)sender;
		} else {
			sender.sendMessage("This command can only be run by a player.");
			return false; // for now, no console commands
		}
		if(cmd.getName().equalsIgnoreCase("shields")){
			FactionShieldOwner shieldsowner = new FactionShieldOwner(faction);
			Shield(shieldsowner);
			return true;
		}
		return false;
	}
	
	private void Shield(FactionShieldOwner shieldowner){
		shieldowner.sendMessage("Shields at %!");
	}
}
