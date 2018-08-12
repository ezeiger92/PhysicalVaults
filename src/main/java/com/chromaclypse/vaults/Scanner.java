package com.chromaclypse.vaults;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.chromaclypse.api.Log;
import com.chromaclypse.vaults.data.VaultStorage.Point;

public class Scanner {
	private Vaults handle;
	
	public Scanner(Vaults handle) {
		this.handle = handle;
	}
	
	private Block toBound(Block block, BlockFace direction) {
		int remaining = handle.getConfig().general.max_dimension;
		
		while(remaining-- > 0 && block.getType() != Material.BEDROCK) {
			block = block.getRelative(direction);
		}
		
		if(remaining >= 0) {
			return block.getRelative(direction.getOppositeFace());
		}
		
		Log.info("Found no block searching in " + direction);
		return null;
	}
	
	public UUID constructVault(String type, Block door, BlockFace searchDirection) {
		
		BlockFace direction = searchDirection;
		Block b1 = door.getRelative(direction);
		Block b2 = toBound(door, direction);
		
		if(b2 == null) {
			return null;
		}
		
		direction = (direction == BlockFace.EAST || direction == BlockFace.WEST) ? BlockFace.NORTH : BlockFace.WEST;

		b1 = toBound(b1, direction);
		b2 = toBound(b2, direction.getOppositeFace());
		
		if(b1 == null || b2 == null) {
			return null;
		}

		b1 = toBound(b1, BlockFace.UP);
		b2 = toBound(b2, BlockFace.DOWN);
		
		if(b1 == null || b2 == null) {
			return null;
		}
		
		Point min = new Point();
		Point max = new Point();

		// Our blocks are on the interior, we need the shell
		min.x = Math.min(b1.getX(), b2.getX()) - 1;
		min.y = Math.min(b1.getY(), b2.getY()) - 1;
		min.z = Math.min(b1.getZ(), b2.getZ()) - 1;

		max.x = Math.max(b1.getX(), b2.getX()) + 1;
		max.y = Math.max(b1.getY(), b2.getY()) + 1;
		max.z = Math.max(b1.getZ(), b2.getZ()) + 1;
		

		Log.info("min: " + min.x + " " + min.y + " " + min.z + " ");
		Log.info("max: " + max.x + " " + max.y + " " + max.z + " ");
		
		Block exit = door.getRelative(searchDirection.getOppositeFace(), 2);
		
		Block iter = door;
		
		while((iter = iter.getRelative(BlockFace.DOWN)).getType() == door.getType()) {
			exit = exit.getRelative(BlockFace.DOWN);
		}
		
		return handle.createVault(type, exit.getLocation(), min, max);
	}
}
