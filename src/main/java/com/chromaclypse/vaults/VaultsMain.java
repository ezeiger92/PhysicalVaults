package com.chromaclypse.vaults;

import org.bukkit.command.TabExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.chromaclypse.api.command.CommandBase;

public class VaultsMain extends JavaPlugin {
	private Vaults vaultManager;
	
	@Override
	public void onEnable() {
		vaultManager = new Vaults(this);
		
		getServer().getPluginManager().registerEvents(new AccessControl(vaultManager), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, vaultManager::queueSave, 20, 20);

		VaultCommands cmd = new VaultCommands(vaultManager);
		
		TabExecutor command = new CommandBase()
				.with().arg("save").calls(cmd::save)
				.with().arg("togglevoid").calls(cmd::togglevoid)
				.with().arg("create").option(cmd::vaultTypes).calls(cmd::create)
				.with().arg("remove").option(cmd::validVaults).calls(cmd::remove)
				.with().arg("rekey").option(cmd::validVaults).calls(cmd::rekey)
				.with().arg("visitorpass").option(cmd::validVaults).option(cmd::passTypes).calls(cmd::visitorpass)
				.getCommand();

		getCommand("phvault").setExecutor(command);
		getCommand("phvault").setTabCompleter(command);
	}
	
	@Override
	public void onDisable() {
		vaultManager.queueSave();
		HandlerList.unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
		
		vaultManager = null;
	}
}
