package com.chromaclypse.vaults;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.api.Log;
import com.chromaclypse.vaults.data.VaultConfig.VaultInfo;

public class VaultCommand implements TabExecutor {
	private Vaults handle;
	
	public VaultCommand(Vaults handle) {
		this.handle = handle;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0) {
			
		}
		else {
			String arg1 = args[0].toLowerCase(Locale.ENGLISH);
			
			switch(arg1) {
				case "create": {// type -> ;
					Log.info("create");
					if(!(sender instanceof Player)) {
						return true;
					}
					
					Player player = (Player) sender;
					
					if(args.length >= 2) {
						Log.info("enough args");
						VaultInfo info = handle.getConfig().vault_types.get(args[1]);
						
						if(info != null) {

							Log.info("found info");
							Block target = player.getTargetBlock(null, 7);
							
							boolean valid = false;
							
							try {
								valid = target.getType() == Material.matchMaterial(info.door_material);
							}
							catch(IllegalArgumentException e) {}
							
							if(valid) {

								Log.info("good door");
								float yaw = player.getLocation().getYaw();
								BlockFace direction;

								yaw -= Math.floor(yaw / 360) * 360;

								if (yaw >= 315 || yaw < 45) {
									direction = BlockFace.SOUTH;
								}
								else if (yaw < 135) {
									direction = BlockFace.WEST;
								}
								else if (yaw < 225) {
									direction = BlockFace.NORTH;
								}
								else {
									direction = BlockFace.EAST;
								}
								
								UUID result = handle.vaultScanner().constructVault(args[1], target, direction);
								

								Log.info("result: " + result);
							}
						}
					}
					else
						Log.info("not enough args");
					
					break;
				}
				
				case "remove": {// vault -> ;
					if(args.length >= 2) {
						Log.info("enough args");
						UUID targetVault;
						
						try {
							targetVault = UUID.fromString(args[1]);
						}
						catch(IllegalArgumentException e) {
							Log.info("bad uuid");
							return true;
						}
						

						Log.info("removing");
						handle.removeVault(targetVault);
					}
					
					break;
				}
				
				case "visitorpass": {// vault, type -> item
					if(!(sender instanceof Player)) {
						return true;
					}
					
					Player player = (Player) sender;
					
					if(args.length >= 3) {
						Log.info("enough args");
						UUID targetVault;
						
						try {
							targetVault = UUID.fromString(args[1]);
						}
						catch(IllegalArgumentException e) {

							Log.info("bad uuid");
							return true;
						}
						

						Log.info("good uuid");
						
						ItemStack keycard = handle.getKeyMaster().createGuestKeycard(targetVault, args[2]);
						
						if(keycard != null) {

							Log.info("created keycard");
							player.getInventory().addItem(keycard);
						}
					}
					
					break;
				}
				
				case "purchase": { // type -> item
					if(!(sender instanceof Player)) {
						return true;
					}
				}
				
				case "resetkeycard": {// vault -> item
					if(!(sender instanceof Player)) {
						return true;
					}
					
					Player player = (Player) sender;
					
					if(args.length >= 2) {
						Log.info("enough args");
						UUID targetVault;
						
						try {
							targetVault = UUID.fromString(args[1]);
						}
						catch(IllegalArgumentException e) {

							Log.info("bad uuid");
							return true;
						}
						

						Log.info("good uuid");
						
						ItemStack keycard = handle.getKeyMaster().resetAndCreateKeycard(targetVault);
						
						if(keycard != null) {

							Log.info("Created new keycard");
							player.getInventory().addItem(keycard);
						}
					}
					
					break;
				}
				
				case "save": {// vault -> item
					handle.save();
					Log.info("saved");
					
					break;
				}
				
				case "void": {// vault -> item
					handle.voidBypass((Player)sender);
					Log.info("Now voiding your bypass settings");
					
					break;
				}
				
				case "unvoid": {// vault -> item
					handle.voidBypass((Player)sender);
					Log.info("Removed void on bypass. Triple negative.");
					
					break;
				}
			}
		}
		return true;
	}

}
