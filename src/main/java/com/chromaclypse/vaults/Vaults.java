package com.chromaclypse.vaults;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.chromaclypse.api.Log;
import com.chromaclypse.api.collision.BlockAabb;
import com.chromaclypse.api.geometry.DynamicAabbTree;
import com.chromaclypse.vaults.data.VaultArchive;
import com.chromaclypse.vaults.data.VaultConfig;
import com.chromaclypse.vaults.data.VaultConfig.VaultInfo;
import com.chromaclypse.vaults.data.VaultStorage;
import com.chromaclypse.vaults.data.VaultStorage.Point;
import com.chromaclypse.vaults.data.VaultStorage.VaultData;

public class Vaults {
	private Plugin handle;
	private boolean needsUpdate = false;
	private VaultConfig config = new VaultConfig();
	private VaultStorage storage = new VaultStorage();
	private VaultArchive archive = new VaultArchive();
	private Map<World, DynamicAabbTree> vaultTree = new HashMap<>();
	private Map<UUID, Integer> vaultToTreeIndex = new HashMap<>();
	private Map<String, Set<UUID>> openVaults = new HashMap<>();
	
	private KeycardFactory keyMaster;
	private Scanner scanner;
	private Particles effects;
	
	public Vaults(Plugin handle) {
		this.handle = handle;
		
		config.init(handle);
		storage.init(handle);
		archive.init(handle);
		
		keyMaster = new KeycardFactory(this);
		scanner = new Scanner(this);
		effects = new Particles(this);
		
		loadStorage();
	}
	
	private DynamicAabbTree getTree(World world) {
		DynamicAabbTree tree = vaultTree.get(world);
		
		if(tree == null) {
			vaultTree.put(world, tree = new DynamicAabbTree(world));
		}
		
		return tree;
	}
	
	private void markEmpty(String type, UUID vault) {
		Set<UUID> vaults = openVaults.get(type);
		
		if(vaults == null) {
			openVaults.put(type, vaults = new HashSet<UUID>());
		}
		
		vaults.add(vault);
	}
	
	private void unmarkEmpty(String type, UUID vault) {
		Set<UUID> vaults = openVaults.get(type);
		
		if(vaults != null) {	
			vaults.remove(vault);
		}
	}
	
	private void loadStorage() {
		Set<String> missingWorlds = new HashSet<>();
		
		for(Map.Entry<String, VaultData> entry : storage.vaults.entrySet()) {
			UUID key;
			try {
				key = UUID.fromString(entry.getKey());
			}
			catch(IllegalArgumentException e) {
				Log.warning("Storage vault with invalid key (" + entry.getKey() + ") detected! Skipping...");
				continue;
			}
			
			VaultData data = entry.getValue();
			World world = handle.getServer().getWorld(data.world_name);
			
			if(world == null) {
				if(missingWorlds.add(data.world_name)) {
					Log.warning("Vault world (" + data.world_name + ") not found on server! Skipping...");
				}
				
				continue;
			}
			
			try {
				UUID.fromString(data.owner_uuid);
			}
			catch(IllegalArgumentException e) {
				markEmpty(data.vault_type, key);
			}
			
			DynamicAabbTree tree = getTree(world);
			
			int index = tree.insertData(new BlockAabb(data.minimum.x, data.minimum.y, data.minimum.z,
					data.maximum.x, data.maximum.y, data.maximum.z),
					key);
			
			vaultToTreeIndex.put(key, index);
		}
	}
	
	public boolean claimVault(UUID owner, UUID vault) {
		VaultData data = getVaultData(vault);
		
		if(data != null) {
			data.owner_uuid = owner.toString();
			unmarkEmpty(data.vault_type, vault);
			return true;
		}
		
		return false;
	}
	
	public UUID createVault(String vaultType, Location exit, Point min, Point max) {
		if(config.vault_types.containsKey(vaultType)) {
			VaultData data = new VaultData();

			data.vault_type = vaultType;
			data.world_name = exit.getWorld().getName();
			
			data.force_exit.x = exit.getBlockX();
			data.force_exit.y = exit.getBlockY();
			data.force_exit.z = exit.getBlockZ();

			data.minimum.x = min.x;
			data.minimum.y = min.y;
			data.minimum.z = min.z;

			data.maximum.x = max.x;
			data.maximum.y = max.y;
			data.maximum.z = max.z;
			
			UUID key = UUID.randomUUID();
			
			markEmpty(vaultType, key);
			
			int index = getTree(exit.getWorld()).insertData(new BlockAabb(min.x, min.y, min.z,
					max.x, max.y, max.z),
					key);
			
			vaultToTreeIndex.put(key, index);
			
			storage.vaults.put(key.toString(), data);
			
			return key;
		}
		
		return null;
	}
	
	public void removeVault(UUID vault) {
		VaultData data = storage.vaults.remove(vault.toString());
		
		if(data != null) {
			// XXX make sure this is always allowed
			int index = vaultToTreeIndex.remove(vault);
			World world = handle.getServer().getWorld(data.world_name);

			unmarkEmpty(data.vault_type, vault);
			
			getTree(world).removeData(index);
		}
	}
	
	public UUID getVaultAt(Location location) {
		List<Object> objects = getTree(location.getWorld()).queryPoint(location);
		
		if(objects.isEmpty()) {
			return null;
		}
		else if(objects.size() > 1) {
			// WARN
		}
		
		return (UUID) objects.get(0);
	}
	
	public VaultData getVaultData(UUID vault) {
		return storage.vaults.get(vault.toString());
	}
	
	public VaultInfo getVaultInfo(UUID vault) {
		VaultData data = getVaultData(vault);
		
		if(data != null) {
			return config.vault_types.get(data.vault_type);
		}
		
		return null;
	}
	
	public boolean hasVaultAt(Location location) {
		return !getTree(location.getWorld()).queryPoint(location).isEmpty();
	}
	
	public void save() {
		needsUpdate = false;
		storage.save(handle);
		archive.save(handle);
	}
	
	public void queueSave() {
		if (needsUpdate) {
			save();
		}
	}
	
	public Plugin getPlugin() {
		return handle;
	}
	
	public Particles effects() {
		return effects;
	}
	
	/////
	public void voidBypass(Player player) {
		voidBypass.add(player.getUniqueId());
	}

	public void unvoidBypass(Player player) {
		voidBypass.remove(player.getUniqueId());
	}
	
	public boolean hasBypass(Player player) {
		return player.hasPermission("phvault.bypass") && !voidBypass.contains(player.getUniqueId());
	}
	
	private Set<UUID> voidBypass = new HashSet<>();
	//////

	public void informUpdate() {
		needsUpdate = true;
	}
	
	public VaultConfig getConfig() {
		return config;
	}
	
	public VaultStorage getStorage() {
		return storage;
	}
	
	public VaultArchive getArchive() {
		return archive;
	}
	
	public KeycardFactory getKeyMaster() {
		return keyMaster;
	}
	
	public Scanner vaultScanner() {
		return scanner;
	}
	
	public static long makeTime(String duration) {
		long time = 0;
		long temp = 0;
		
		for(char c : duration.toLowerCase(Locale.ENGLISH).toCharArray()) {
			switch(c) {
				case 'y':
					time += temp * 24 * 365 * 60 * 60;
					temp = 0;
					break;
					
				case 'M':
					time += temp * 24 * 30 * 60 * 60;
					temp = 0;
					break;
					
				case 'w':
					time += temp * 24 * 7 * 60 * 60;
					temp = 0;
					break;
				case 'd':
					time += temp * 24 * 60 * 60;
					temp = 0;
					break;
					
				case 'h':
					time += temp * 60 * 60;
					temp = 0;
					break;
					
				case 'm':
					time += temp * 60;
					temp = 0;
					break;
					
				case 's':
					time += temp;
					temp = 0;
					break;
					
				default:
					temp = temp * 10 + (c - '0');
					break;
			}
		}
		
		return time + temp;
	}
	
	public static long currentTime() {
		return System.currentTimeMillis() / 1000;
	}
}
