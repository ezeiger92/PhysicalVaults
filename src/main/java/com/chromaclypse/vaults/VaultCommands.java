package com.chromaclypse.vaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.api.command.Context;
import com.chromaclypse.api.messages.Text;
import com.chromaclypse.vaults.data.VaultConfig.VaultInfo;

public class VaultCommands {
	private final Vaults handle;
	
	private static final <T> T NotNull(T object, String message) {
		if(object != null) {
			return object;
		}
		
		throw new NullPointerException(message);
	}
	
	private static final BlockFace GetFacing(Player player) {
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
		
		return direction;
	}
	
	public VaultCommands(Vaults handle) {
		this.handle = handle;
	}
	
	public List<String> vaultTypes(Context create) {
		return new ArrayList<>(handle.getConfig().vault_types.keySet());
	}
	
	public List<String> validVaults(Context create) {
		return new ArrayList<>(handle.getStorage().vaults.keySet());
	}
	
	public List<String> passTypes(Context create) {
		return new ArrayList<>(handle.getConfig().keycards.guest_keycards.keySet());
	}

	public boolean create(Context command) {
		Player player = command.Player();
		String type = command.GetArg(1);
		VaultInfo info = NotNull(handle.getConfig().vault_types.get(type), "No vault info not found for "+ type);
		Block target = NotNull(player.getTargetBlockExact(7), "No target block found (too far away?)");
		
		if(target.getType() != Material.matchMaterial(info.door_material)) {
			throw new IllegalArgumentException("Target block does not match expected door material");
		}
		
		UUID result = NotNull(handle.vaultScanner().constructVault(type, target, GetFacing(player)), "Failed to construct vault!");
		
		player.sendMessage(Text.format().colorize("&aCreated new vault with id: " + result));
		return true;
	}

	public boolean remove(Context command) {
		String vault = command.GetArg(1);
		handle.removeVault(UUID.fromString(vault));
		
		command.Sender().sendMessage(Text.format().colorize("&aRemoved vault: " + vault));
		return true;
	}
	
	public boolean visitorpass(Context command) {
		Player player = command.Player();
		UUID vault = command.GetArg(1, UUID::fromString);
		String type = command.GetArg(2);
		
		ItemStack keycard = NotNull(handle.getKeyMaster().createGuestKeycard(vault, type), "Failed to create keycard!");
		
		player.getInventory().addItem(keycard);
		player.sendMessage(Text.format().colorize("&aTemporary [" + type + "] keycard created for vault: " + vault));
		return true;
	}
	
	public boolean rekey(Context command) {
		Player player = command.Player();
		UUID vault = command.GetArg(1, UUID::fromString);
		
		ItemStack keycard = NotNull(handle.getKeyMaster().resetAndCreateKeycard(vault), "Failed to create keycard!");
		
		player.getInventory().addItem(keycard);
		player.sendMessage(Text.format().colorize("&aReset vault codes for vault: " + vault));
		return true;
	}
	
	public boolean save(Context command) {
		handle.save();
		command.Sender().sendMessage(Text.format().colorize("&aVault data saved"));
		return true;
	}
	
	public boolean togglevoid(Context command) {
		Player player = command.Player();
		if(handle.hasBypass(player)) {
			handle.voidBypass(command.Player());
			command.Sender().sendMessage(Text.format().colorize("&eSimulating plebian permissions!"));
		}
		else {
			handle.unvoidBypass(command.Player());
			command.Sender().sendMessage(Text.format().colorize("&aRestoring elevated permissions"));
		}
		
		return true;
	}
}
