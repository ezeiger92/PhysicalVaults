package com.chromaclypse.vaults;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.chromaclypse.api.command.Dispatch;

public class VaultsMain extends JavaPlugin {
	private Vaults vaultManager;
	
	@Override
	public void onEnable() {
		vaultManager = new Vaults(this);
		
		getServer().getPluginManager().registerEvents(new AccessControl(vaultManager), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, vaultManager::queueSave, 20, 20);

		PluginCommand cmd = getCommand("phvault");

		cmd.setExecutor(new Dispatch(new VaultCommands(vaultManager)));
	}
	
	@Override
	public void onDisable() {
		vaultManager.queueSave();
		HandlerList.unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
		
		vaultManager = null;
	}
}
