package com.chromaclypse.vaults;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultsMain extends JavaPlugin {
	private Vaults vaultManager;
	
	@Override
	public void onEnable() {
		vaultManager = new Vaults(this);
		VaultCommand command = new VaultCommand(vaultManager);
		
		getServer().getPluginManager().registerEvents(new AccessControl(vaultManager), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, vaultManager::queueSave, 20, 20);

		PluginCommand cmd = getCommand("phvault");

		cmd.setExecutor(command);
		cmd.setTabCompleter(command);
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
		
		vaultManager = null;
	}
}
