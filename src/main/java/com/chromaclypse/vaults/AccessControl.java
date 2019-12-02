package com.chromaclypse.vaults;

import java.util.EnumSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.api.Log;
import com.chromaclypse.vaults.data.VaultConfig.VaultInfo;
import com.chromaclypse.vaults.data.VaultStorage.Point;
import com.chromaclypse.vaults.data.VaultStorage.VaultData;

public class AccessControl implements Listener {
	private Vaults handle;
	
	public AccessControl(Vaults handle) {
		this.handle = handle;
	}
	
	public boolean hasAccess(Player player, UUID vault) {
		if(handle.hasBypass(player)) {
			return true;
		}
		
		VaultData data = handle.getVaultData(vault);
		
		if(data == null) {
			return false;
		}
		
		for(ItemStack is : player.getInventory().getStorageContents()) {
			if(is != null) {
				Keycard keycard = handle.getKeyMaster().GetKeycard(is);
				
				if(keycard != null) {
					if(vault.equals(keycard.getVault()) && data.keycard_registered_at <= keycard.getCreated() &&
							!keycard.hasExpired()) {
						return true;
					}
					else {
						handle.getKeyMaster().updateKeycard(is, null);
					}
				}
			}
		}
		
		return false;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Location ploc = player.getLocation();
		UUID vault = handle.getVaultAt(ploc);
		
		if(vault != null && !hasAccess(player, vault)) {
			Point exit = handle.getVaultData(vault).force_exit;

			ploc.setX(exit.x + 0.5);
			ploc.setY(exit.y);
			ploc.setZ(exit.z + 0.5);
			
			player.teleport(ploc);
		}
	}
	
	private static OfflinePlayer getOwner(VaultData data) {
		try {
			OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(data.owner_uuid));
			
			if(owner.hasPlayedBefore()) {
				return owner;
			}
		}
		catch(IllegalArgumentException e) {}
		
		return null;
	}
	
	private static String ownerDisplayName(Player player, OfflinePlayer owner) {
		if(owner == null) {
			return "empty";
		}
		else if(player.getUniqueId().equals(owner.getUniqueId())) {
			return "your";
		}
		else {
			String ownerName = owner.getName();
			
			if(ownerName.endsWith("s") || ownerName.endsWith("S")) {
				return ownerName + "'";
			}
			else {
				return ownerName + "'s";
			}
		}
	}

	private static String enterMessage(Player player, VaultData data) {
		return "Entering " + ownerDisplayName(player,  getOwner(data)) + " vault";
	}
	
	private static String exitMessage(Player player, VaultData data) {
		return "Left " + ownerDisplayName(player,  getOwner(data)) + " vault";
	}
	
	private static final EnumSet<EntityType> protectedEntities = EnumSet.of(
			EntityType.ARMOR_STAND,
			EntityType.PAINTING,
			EntityType.ITEM_FRAME);
	
	@EventHandler
	public void vaultEntityInteract(PlayerInteractEntityEvent event) {
		EntityType type = event.getRightClicked().getType();
		
		if(!protectedEntities.contains(type)) {
			return;
		}
		
		UUID vault = handle.getVaultAt(event.getRightClicked().getLocation());

		if(vault == null) {
			return;
		}
		
		// There's no reason to interact at a painting, so avoid craftbook issues
		if(type == EntityType.PAINTING || !hasAccess(event.getPlayer(), vault)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void vaultEntityDamage(EntityDamageEvent event) {
		if(!protectedEntities.contains(event.getEntityType()) ||
				handle.getVaultAt(event.getEntity().getLocation()) == null) {
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void vaultGuide(PlayerInteractEvent event) {
		if(event.getHand() == EquipmentSlot.HAND) {
			Log.info("useInteracted: " + event.useInteractedBlock());
			Log.info("useHand: " + event.useItemInHand());
			
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				UUID clickedVault = handle.getVaultAt(event.getPlayer().getLocation());
				
				if(clickedVault != null) {
					return;
				}
				
				Keycard keycard = handle.getKeyMaster().GetKeycard(event.getPlayer().getInventory().getItemInMainHand());
				
				if(keycard == null) {
					return;
				}
				
				VaultData data = handle.getVaultData(keycard.getVault());
				
				if(data != null) {
					Location to = new Location(Bukkit.getWorld(data.world_name),
							data.force_exit.x + 0.5, data.force_exit.y + 1.5, data.force_exit.z + 0.5);
					
					Location from = event.getPlayer().getEyeLocation();
					from.add(from.getDirection().multiply(1.5));
					
					handle.effects().makePathFor(from, to, event.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void vaultDoor(PlayerInteractEvent event) {
		if(event.getHand() == EquipmentSlot.OFF_HAND) {
			return;
		}
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			UUID clickedVault = handle.getVaultAt(event.getClickedBlock().getLocation());
			
			if(clickedVault == null) {
				return;
			}
			
			VaultInfo info = handle.getVaultInfo(clickedVault);
			
			Material mat = event.getClickedBlock().getType();
			boolean valid = false;
			
			try {
				if (Material.matchMaterial(info.door_material) == mat) {
					valid = true;
				}
			}
			catch(IllegalArgumentException e) {}
			
			if(valid) {
				Player player = event.getPlayer();
				Location ploc = player.getLocation();
				VaultData data = handle.getVaultData(clickedVault);
				
				UUID standingVault = handle.getVaultAt(ploc);
				
				BlockFace direction = event.getBlockFace().getOppositeFace();
				
				String message;
				if(clickedVault.equals(standingVault)) {
					// exiting
					
					message = exitMessage(player, data);
				}
				else {
					// entering
					if(!hasAccess(player, clickedVault)) {
						player.sendMessage("You aren't holding a keycard for this vault, did you misplace it?");
						return;
					}
					
					message = enterMessage(player, data);
				}

				Location landing = event.getClickedBlock().getRelative(direction).getLocation().add(0.5, 0.0, 0.5);
				
				Block iter = event.getClickedBlock();
				
				while((iter = iter.getRelative(BlockFace.DOWN)).getType() == mat) {
					landing.subtract(0, 1, 0);
				}
				
				landing.setYaw(ploc.getYaw());
				landing.setPitch(ploc.getPitch());
				
				player.teleport(landing);
				player.sendMessage(message);
				
				event.setCancelled(true);
			}
		}
	}
}
