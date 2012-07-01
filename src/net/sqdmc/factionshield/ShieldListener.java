package net.sqdmc.factionshield;

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
	
	private HashMap<Integer, Integer> ShieldDurability = new HashMap<Integer, Integer>();
	private HashMap<Integer, Timer> shieldTimer = new HashMap<Integer, Timer>();
	
	private HashMap<ShieldOwner, Shield> shields = new HashMap<ShieldOwner, Shield>();
	private HashMap<Block, ShieldBase> blockShieldBase = new HashMap<Block, ShieldBase>();
	//private Map<Shield, ShieldOwner> sh = new HashMap<Shield, ShieldOwner>();
	
	public ShieldListener(FactionShield plugin) {
		this.plugin = plugin;
		config = plugin.getFSconfig();
	}
	
	@EventHandler
	public void createShield(SignChangeEvent event) {
		Player player = event.getPlayer();
		Faction faction = Board.getFactionAt(event.getBlock().getLocation());
		FPlayer fPlayer = new FPlayer();
					
		FactionShieldOwner fshieldowner;
		
		String line0 = event.getLine(0);
		if (line0.equals("[shield]")) {
			fshieldowner = new FactionShieldOwner(faction);
			event.setLine(1, player.getName());
			event.setLine(2, faction.getTag());
		} else return; // not for us!

		Block signBlock = event.getBlock();
		Block ShieldBlock = signBlock.getRelative(BlockFace.DOWN);
		
		if (faction.getId().equals("-2") || faction.getId().equals("-1")  || faction.getId().equals("0") )
		{
			signBlock.breakNaturally();
			return;
		}
		
		if (ShieldBlock.getType() == Material.SPONGE) {
			
			Block Sponge = (Block)ShieldBlock;	
			
			if (blockShieldBase != null){
				//if (shield.getOwner().getId() == shields.get(fshieldowner).owner.getId()){
				if (shields.containsKey(fshieldowner)){
				//if (shields.equals(fshieldowner))
					log.info("Already have a shield");
					Sponge.breakNaturally();
					return;
				}
			}
			
			Shield shield = getShield(fshieldowner);
						
			ShieldBase shieldbase = new ShieldBase(Sponge, (Sign)signBlock.getState(), shield, ShieldBlock.getWorld(),ShieldBlock.getX(),ShieldBlock.getY(),ShieldBlock.getZ());
			
			log.info(fshieldowner.toString());
			
			blockShieldBase.put(signBlock, shieldbase);
			blockShieldBase.put(ShieldBlock, shieldbase);
			
			faction.setPowerLoss(-config.getPowerCost());
			
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
		ShieldBase shieldBase = blockShieldBase.get(block);
		if (shieldBase != null) {
			Shield shield = shieldBase.shield;
			shieldBase.destroy();
			blockShieldBase.remove(shieldBase.sponge);
			blockShieldBase.remove(shieldBase.sign);
			FactionShieldOwner fShieldOwner = new FactionShieldOwner(Board.getFactionAt(block));
			shields.remove(fShieldOwner);
			//blockShieldBase.remove(fShieldOwner);
			
			Faction faction = Board.getFactionAt(event.getBlock().getLocation());
			faction.setPowerLoss(0);
			
			shield.owner.sendMessage("Shield Destroyed!");
		}
	}
	
	private void TNTBreakShield(Block targetloc, Block shieldblock)
	{
		//Block block = event.getBlock();
		ShieldBase shieldBase = blockShieldBase.get(targetloc);
		if (shieldBase != null) {
			Shield shield = shieldBase.shield;
			shieldBase.destroy();
			blockShieldBase.remove(shieldBase.sponge);
			blockShieldBase.remove(shieldBase.sign);
			FactionShieldOwner fShieldOwner = new FactionShieldOwner(Board.getFactionAt(shieldblock));
			shields.remove(fShieldOwner);
			//blockShieldBase.remove(fShieldOwner);
			
			//signblock.breakNaturally();
			//shieldblock.breakNaturally();
			
			shieldblock.getWorld().createExplosion(targetloc.getLocation(), 10, true);
			
			Faction faction = Board.getFactionAt(targetloc.getLocation());
			faction.setPowerLoss(0);
			
			shield.owner.sendMessage("Shield Destroyed!");
		}		
	}

	public Shield getShield(ShieldOwner owner) {
		Shield shield = shields.get(owner);
		if (shield == null) {
			shield =  new Shield(owner);
			shields.put(owner,shield);
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

		if (detonator == null) {
			return;
		}
		
		final Location detonatorLoc = detonator.getLocation();
			
		// calculate sphere around detonator
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					Location targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);

					if (detonatorLoc.distance(targetLoc) <= radius) {				
						
						Block signBlock = targetLoc.getBlock();
						if (signBlock.getType() == Material.SIGN || signBlock.getType() == Material.SIGN_POST){
							log.info("TNT Exploded");
							
							Block ShieldBlock = signBlock.getRelative(BlockFace.DOWN);
							if (ShieldBlock.getType() == Material.SPONGE) {
							
								//FactionShieldOwner fshieldowner = new FactionShieldOwner();
								
								Faction faction = Board.getFactionAt(targetLoc);
								
							    Sign s = (Sign) signBlock.getState();
							    String shi = s.getLine(0);
							    String p = s.getLine(1);
							    String fp = s.getLine(2);
							    
							    Player player = null;
							    player = Bukkit.getPlayerExact(p);

							    if ((shi != "[shield]" || shi.replace(" ", "") != "[shield]") && player == null)
							    {
							    	return;
							    }							    						   
							    
							    //sh.get(player);
							    
							    FactionShieldOwner fSheildowner = new FactionShieldOwner(faction);

								Shield shield = new Shield(fSheildowner);
								
								ShieldBase shieldBase = new ShieldBase(ShieldBlock, s, shield, targetLoc.getWorld(), targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
																
								
								String shieldlocation = targetLoc.getWorld() + "," + targetLoc.getBlockX() + "," + targetLoc.getBlockY() + "," + targetLoc.getBlockZ();

								//log.info(shieldBase.getShieldBaseLoc());
								//log.info(shieldlocation);
								
								if (shieldBase.getShieldBaseLoc().equals(shieldlocation))
								{					
									Integer representation = targetLoc.getWorld().hashCode() + targetLoc.getBlockX() * 2389 + targetLoc.getBlockY() * 4027 + targetLoc.getBlockZ() * 2053;						
									
									//log.info(representation.toString());
									
									double fPower = faction.getPower();
									//double fPowerMax = faction.getPowerMax();
									double fLandPower = faction.getLandRounded();
								
									double loss = faction.getPowerLoss();	
									if (fPower > fLandPower)
										loss -= 1;
									else
										ShieldBlock.breakNaturally();
										
									faction.setPowerLoss(loss);
									
									
									if (ShieldDurability.containsKey(representation)) {

										int currentDurability = (int) ShieldDurability.get(representation);
										currentDurability++;
										
										if (checkIfMax(currentDurability)) {
											// counter has reached max durability, so remove the
											// block and drop an item
											//log.info("Hit Max Shield Dura");
											TNTBreakShield(targetLoc.getBlock(), ShieldBlock);
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
											//ShieldBlock.breakNaturally();
											TNTBreakShield(targetLoc.getBlock(), ShieldBlock);
											//faction.setPowerLoss(0);
											//RegenPowerLoss();
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
		
	public void RegenPowerLoss()
	{
		Faction faction = new Faction();
		
		double fPower = faction.getPower();
		double fPowerMax = faction.getPowerMax();
		//double fLandPower = faction.getLandRounded();
		
		double loss = faction.getPowerLoss();	
		
		//if (loss < 0 && fPower < fPowerMax)
			//loss += 1;
		
		loss = 0;
		
		faction.setPowerLoss(loss);
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
		return shields;
	}
	
	public void setShields(HashMap<ShieldOwner, Shield> map) {
		if (map == null) {
			return;
		}

		shields = map;
	}

	public HashMap<Block, ShieldBase> getShieldsBase() {
		// TODO Auto-generated method stub
		return blockShieldBase;
	}
	
	public void setShieldBase(HashMap<Block, ShieldBase> map) {
		if (map == null) {
			return;
		}

		blockShieldBase = map;
	}

}
