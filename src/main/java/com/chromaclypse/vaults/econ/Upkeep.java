package com.chromaclypse.vaults.econ;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.chromaclypse.vaults.Vaults;

public class Upkeep implements Listener {
	private final Vaults handle;
	
	public Upkeep(Vaults handle) {
		this.handle = handle;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
	}
}
