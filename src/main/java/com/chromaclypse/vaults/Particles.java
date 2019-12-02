package com.chromaclypse.vaults;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Particles {
	private final Vaults handle;
	private final Particle.DustOptions options;

	public Particles(Vaults handle) {
		this.handle = handle;
		options = new Particle.DustOptions(Color.fromRGB(25, 240, 51), 1);
	}
	
	public void makePathFor(Location from, Location to, Player... players) {
		
		if(to.getWorld() != from.getWorld()) {
			return;
		}
		
		double distance = to.distance(from);
		
		double tScale = 0.5 / distance;
		
		Vector delta = to.toVector().subtract(from.toVector()).multiply(tScale);
		from.subtract(delta);
		
		for(Player player : players) {
			
			new BukkitRunnable() {
				private double t = -tScale;

				@Override
				public void run() {
					if(!player.isOnline()) {
						cancel();
						return;
					}
					
					t += tScale;
					Location target = from.add(delta);
					
					if(t >= 1) {
						target = to;
						cancel();
					}
					
					player.spawnParticle(Particle.REDSTONE, target, 0, options);
				}
				
			}.runTaskTimer(handle.getPlugin(), 0, 1);
		}
	}
}
