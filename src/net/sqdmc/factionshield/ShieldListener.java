package net.sqdmc.factionshield;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Board;

import net.sqdmc.factionshield.Shield;
import net.sqdmc.factionshield.FactionShieldOwner;

public class ShieldListener implements Listener {
	
	private FactionShield plugin;
	private FSconfig config;
	
	private Logger log = Bukkit.getServer().getLogger();
	private ShieldStorage shieldstorage;
		
	private HashMap<Integer, Integer> ShieldDurability = new HashMap<Integer, Integer>();
	private HashMap<Integer, Timer> shieldTimer = new HashMap<Integer, Timer>();
	
	public ShieldListener(FactionShield plugin) {
		this.plugin = plugin;
		config = plugin.getFSconfig();
		if (shieldstorage != null)
		{
			//config.deserialize();
		}
		else
		{
			shieldstorage = new ShieldStorage();
			//config.deserialize();
		}
	}
	
	@EventHandler
	public void createShield(SignChangeEvent event) {
		Player player = event.getPlayer();
		Faction faction = Board.getFactionAt(event.getBlock().getLocation());
		//FPlayer fPlayer = new FPlayer();
					
		FactionShieldOwner fshieldowner;
		
		String line0 = event.getLine(0);
		String line1 = event.getLine(1);
		String shieldPower = line1;
		if (line0.equalsIgnoreCase("[shield]") && (line1 != null && !line1.equals("") )) {		
			fshieldowner = new FactionShieldOwner(faction);
			event.setLine(1, player.getName());
			event.setLine(2, faction.getTag());
			event.setLine(3, shieldPower);
		} else return; // not for us!
		
		Block signBlock = event.getBlock();
		Block ShieldBlock = signBlock.getRelative(BlockFace.DOWN);
		
		if (!isNumeric(shieldPower)) {
			signBlock.breakNaturally();
			return;
		}
		if (Integer.parseInt(shieldPower) < 1) {
			signBlock.breakNaturally();
			return;
		}
		if (faction.getId().equals("-2") || faction.getId().equals("-1")  || faction.getId().equals("0") ) {
			signBlock.breakNaturally();
			return;
		}
		
		if (ShieldBlock.getType() == Material.SPONGE) {
			
			Block Sponge = (Block)ShieldBlock;	
			
			if (Integer.parseInt(shieldPower) > config.getMaxPowerCost() || Integer.parseInt(shieldPower) > faction.getPower())
			{
				log.info("Not enough power to create Shield for player "+ player.getName());
				fshieldowner.sendMessage("Not enough power to create Shield!");
				signBlock.breakNaturally();
				Sponge.breakNaturally();
				return;			
			}
			
			if (shieldstorage.getBlockShieldBase() != null){
				if (shieldstorage.getShields().containsKey(fshieldowner)){
					log.info("Already have a shield");
					Sponge.breakNaturally();
					return;
				}
			}
			
			Shield shield = getShield(fshieldowner);
			
			shield.setShieldPower(Integer.parseInt(shieldPower));
			shield.setMaxShieldPower(Integer.parseInt(shieldPower));
						
			ShieldBase shieldbase = new ShieldBase(Sponge, signBlock, shield, ShieldBlock.getWorld(),ShieldBlock.getX(),ShieldBlock.getY(),ShieldBlock.getZ());
			
			log.info(fshieldowner.toString());
			
			shieldstorage.addBlockShieldBase(signBlock, shieldbase);
			shieldstorage.addBlockShieldBase(ShieldBlock, shieldbase);
			
			faction.setPowerLoss(-Integer.parseInt(shieldPower));
			
			/*Integer representation = ShieldBlock.getWorld().hashCode() + ShieldBlock.getX() * 2389 + ShieldBlock.getY() * 4027 + ShieldBlock.getZ() * 2053;						
			
			ShieldDurability.put(representation, 3);
			setShieldDurability(ShieldDurability);*/
		
			log.info("Sheild created by "+ player.getName());
			fshieldowner.sendMessage("Shield Created");
		}
		
	}
	
	@EventHandler
	public void shieldBroken(BlockBreakEvent event) {
		Block block = event.getBlock();
		ShieldBase shieldBase = shieldstorage.getBlockShieldBase().get(block);
		if (shieldBase != null) {
			Shield shield = shieldBase.shield;
			shieldBase.destroy();
			FactionShieldOwner fShieldOwner = new FactionShieldOwner(Board.getFactionAt(block));
			shieldstorage.removeShields(fShieldOwner);
			shieldstorage.removeBlockShieldBase(shieldBase.sponge);
			shieldstorage.removeBlockShieldBase(shieldBase.sign);
			
			Faction faction = Board.getFactionAt(event.getBlock().getLocation());
			faction.setPowerLoss(0);
			
			shield.owner.sendMessage("Shield Destroyed!");
		}
	}
	
	private void TNTBreakShield(Block shieldblock)
	{
		ShieldBase shieldBase = shieldstorage.getBlockShieldBase().get(shieldblock);	
		if (shieldBase != null) {
			Shield shield = shieldBase.shield;
			FactionShieldOwner fShieldOwner = new FactionShieldOwner(Board.getFactionAt(shieldblock));
			shieldstorage.removeShields(fShieldOwner);
			shieldstorage.removeBlockShieldBase(shieldBase.sponge);
			shieldstorage.removeBlockShieldBase(shieldBase.sign);
			//shieldBase.destroy();
			
			int explosionpower = Math.round(shieldBase.shield.getShieldPowerMax() / 4);
			//log.info("Explosion Power: " + explosionpower + "  MaxShield: " + shieldBase.shield.getShieldPowerMax());
			
			if (explosionpower >= 11)
				explosionpower = 11;
			else if (explosionpower <= 2)
				explosionpower = 2;
			
			log.info("Shield exploded with Explosion Power of: " + explosionpower);
			
			shieldblock.getWorld().createExplosion(shieldblock.getLocation(), explosionpower, true);
			shieldblock.breakNaturally();
			
			Faction faction = Board.getFactionAt(shieldblock);
			shield.owner.sendMessage("Shield Destroyed!");
			
			faction.setPowerLoss(0);
		}		
	}

	public Shield getShield(ShieldOwner owner) {
		Shield shield = shieldstorage.getShields().get(owner);
		if (shield == null) {
			shield =  new Shield(owner);
			//shield.setShieldPower(100);
			//shield.setMaxShieldPower(100);
			shieldstorage.addShield(owner,shield);
		}
		
		return shield;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (event == null || event.isCancelled()) {
			return;
		}

		
		final int radius = config.getProtRadius();
		
		final Entity detonator = event.getEntity();
		final Location detonatorLoc;

		if (detonator == null) {
			log.info("Det Null!");
			detonatorLoc = event.getLocation();
			return;
		}
		else {	
			detonatorLoc = detonator.getLocation();
		}
			
		// calculate sphere around detonator
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					Location targetLoc;
					if (detonator != null) {
						targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);
					}
					else {
						targetLoc = new Location(event.getLocation().getWorld(), event.getLocation().getX() + x, event.getLocation().getY() + y, event.getLocation().getZ() + z);
					}

					if (detonatorLoc.distance(targetLoc) <= radius) {				
						
						Block signBlock = targetLoc.getBlock();
						if (signBlock.getType() == Material.SIGN || signBlock.getType() == Material.SIGN_POST){
							//log.info("TNT Exploded");
							
							Block ShieldBlock = signBlock.getRelative(BlockFace.DOWN);
							if (ShieldBlock.getType() == Material.SPONGE) {				
								Faction faction = Board.getFactionAt(targetLoc);
								
							    Sign s = (Sign) signBlock.getState();
							    String shi = s.getLine(0);
							    String p = s.getLine(1);
							    //String fp = s.getLine(2);
							    int pow = Integer.parseInt(s.getLine(3));
							    
							    Player player = null;
							    player = Bukkit.getPlayerExact(p);

							    if ((shi != "[shield]" || shi.replace(" ", "") != "[shield]") && player == null)
							    {
							    	return;
							    }							    						   
							    
							    FactionShieldOwner fSheildowner = new FactionShieldOwner(faction);
								Shield shield = new Shield(fSheildowner);
								ShieldBase shieldBase = new ShieldBase(ShieldBlock, signBlock, shield, targetLoc.getWorld(), targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
								
								String shieldlocation = targetLoc.getWorld().getName() + "," + targetLoc.getBlockX() + "," + targetLoc.getBlockY() + "," + targetLoc.getBlockZ();
								
								if (shieldBase.getShieldBaseLoc().equals(shieldlocation))
								{					
									Integer representation = targetLoc.getWorld().hashCode() + targetLoc.getBlockX() * 2389 + targetLoc.getBlockY() * 4027 + targetLoc.getBlockZ() * 2053;						
									
									pow--;
									shield.setShieldPower(pow);
									
									if (pow <= 0)
										TNTBreakShield(ShieldBlock);
									if (pow > 0){
										String newpower = String.valueOf(pow);
										s.setLine(3, newpower);
										s.update();
										shield.owner.sendMessage("Shield Power at " + newpower + "!");
									}
									else
										TNTBreakShield(ShieldBlock);
										
									
									if (ShieldDurability.containsKey(representation)) {

										int currentDurability = (int) ShieldDurability.get(representation);
										currentDurability++;
										
										if (checkIfMax(currentDurability)) {
											// counter has reached max durability
											//log.info("Hit Max Shield Dura");
											TNTBreakShield(ShieldBlock);
											faction.setPowerLoss(0);
											ResetTime(representation, targetLoc);
											return;
										} else {
											// counter has not reached max durability yet
											ShieldDurability.put(representation, currentDurability);
											//log.info("Set already, set Shield Dura");

											startNewTimer(representation);
										}
									} else {
										ShieldDurability.put(representation, 1);
										//log.info("Set New Shield Dura");
										startNewTimer(representation);

										if (checkIfMax(1)) {
											TNTBreakShield(ShieldBlock);
											ResetTime(representation, targetLoc);
											//log.info("Hit Max");
										}
									}

									//log.info(faction.getTag());
									//log.info("TNT Denied.");
									
									event.setCancelled(true);
									return;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static boolean isNumeric(String str)
	{
	  NumberFormat formatter = NumberFormat.getInstance();
	  ParsePosition pos = new ParsePosition(0);
	  formatter.parse(str, pos);
	  return str.length() == pos.getIndex();
	}
		
	public void RegenPowerLoss(ShieldBase shieldBase)
	{
		if (shieldBase != null) {
			int max = shieldBase.shield.getShieldPowerMax();
		
			shieldBase.shield.setShieldPower(max);
			Sign sign = (Sign) shieldBase.sign.getState();
			String newpower = String.valueOf(max);	
			sign.setLine(3, newpower);
			sign.update();
		}
	}
	
	private void startNewTimer(Integer representation) {
		if (shieldTimer.get(representation) != null) {
			shieldTimer.get(representation).cancel();
		}

		Timer timer = new Timer();
		timer.schedule(new ShieldTimer(plugin, representation), config.getRegenTime());

		shieldTimer.put(representation, timer);
	}
	
	private boolean checkIfMax(int value) {
		return value == config.getDurability();
	}
	
	private void ResetTime(Integer representation, Location at) {
		ShieldDurability.remove(representation);
		//destroyBlockAndDropItem(at);

			if (shieldTimer.get(representation) != null) {
				shieldTimer.get(representation).cancel();
			}

			shieldTimer.remove(representation);
	}
	
	/**
	 * Returns the HashMap containing all saved durabilities.
	 * 
	 * @return the HashMap containing all saved durabilities
	 */
	public HashMap<Integer, Integer> getShieldDurability() {
		return ShieldDurability;
	}
	
	/**
	 * Sets the HashMap containing all saved durabilities.
	 * 
	 * @param map
	 *            the HashMap containing all saved durabilities
	 */
	public void setShieldDurability(HashMap<Integer, Integer> map) {
		if (map == null) {
			return;
		}

		ShieldDurability = map;
	}

	/**
	 * Returns the HashMap containing all saved durability timers.
	 * 
	 * @return the HashMap containing all saved durability timers
	 */
	public HashMap<Integer, Timer> getObsidianTimer() {
		return shieldTimer;
	}

	/**
	 * Sets the HashMap containing all saved durability timers.
	 * 
	 * @param map
	 *            the HashMap containing all saved durability timers
	 */
	public void setShieldTimer(HashMap<Integer, Timer> map) {
		if (map == null) {
			return;
		}

		shieldTimer = map;
	}

	
	public HashMap<ShieldOwner, Shield> getShields() {
		// TODO Auto-generated method stub
		return shieldstorage.getShields();
	}
	
	public void setShields(HashMap<ShieldOwner, Shield> map) {
		if (map == null) {
			return;
		}
		
		shieldstorage.setShields(map);

		//shields = map;
	}

	public HashMap<Block, ShieldBase> getShieldsBase() {
		// TODO Auto-generated method stub
		return shieldstorage.getBlockShieldBase();
	}
	
	public void setShieldBase(HashMap<Block, ShieldBase> map) {
		if (map == null) {
			return;
		}

		shieldstorage.setBlockShieldBase(map);
		//blockShieldBase = map;
	}

}
