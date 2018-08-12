package com.chromaclypse.vaults;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chromaclypse.api.item.ItemBuilder;
import com.chromaclypse.api.messages.Text;
import com.chromaclypse.vaults.data.VaultConfig.KeycardConfig.AccessCardInfo;
import com.chromaclypse.vaults.data.VaultStorage.VaultData;

public class KeycardFactory {
	private Vaults handle;
	
	public KeycardFactory(Vaults handle) {
		this.handle = handle;
	}
	
	private static void encodeLongTo(StringBuilder output, long input) {
		long power = 1;
		long temp = input;
		
		while(temp >= 10) {
			power *= 10;
			temp /= 10;
		}
		
		while(power > 0) {
			output.append('&').append((input / power) % 10);
			power /= 10;
		}
	}
	
	private static String createKeycardMeta(UUID vault, long created, long expiry) {
		StringBuilder result = new StringBuilder();
		for(char c : vault.toString().toLowerCase().toCharArray()) {
			result.append('&').append(c);
		}
		
		result.append("&r");
		encodeLongTo(result, created);
		
		if(expiry >= 0) {
			result.append("&r");
			encodeLongTo(result, expiry);
		}
		
		String str = result.toString().replace('-', 'm');

		return str;
	}
	
	private static String[] metaParts(ItemStack keycard) {
		if(keycard.hasItemMeta()) {
			ItemMeta meta = keycard.getItemMeta();
			
			if(meta.hasLore()) {
				String encodedSeparator = Text.colorize("&r");
				String[] parts = meta.getLore().get(0).split(encodedSeparator);
				
				if(parts.length == 2 || parts.length == 3) {
					return parts;
				}
			}
		}
		
		return new String[0];
	}
	
	public static Keycard getKeycard(ItemStack keycard) {
		String[] metaParts = metaParts(keycard);
		
		if(metaParts.length >= 2) {
			UUID vault;
			long created;
			
			try {
				vault = UUID.fromString(metaParts[0].replace(String.valueOf(ChatColor.COLOR_CHAR), "")
						.replace('m', '-'));
				created = Long.parseLong(metaParts[1].replace(String.valueOf(ChatColor.COLOR_CHAR), ""));
			}
			catch(IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			}
			
			if(metaParts.length >= 3) {
				long expiry;
				try {
					expiry = Long.parseLong(metaParts[2].replace(String.valueOf(ChatColor.COLOR_CHAR), ""));
				}
				catch(IllegalArgumentException e) {
					return null;
				}
				
				return new Keycard(vault, created, expiry);
			}
			else {
				return new Keycard(vault, created);
			}
		}
		
		return null;
	}
	
	public static void invalidate(ItemStack keycard) {
		ItemBuilder.edit(keycard).lore("&4&l&o-VOID-");
	}
	
	public static UUID getVaultAccess(ItemStack keycard) {
		Keycard card = getKeycard(keycard);
		
		if(card != null) {
			return card.getVault();
		}
		
		return null;
	}
	
	public ItemStack createGuestKeycard(UUID vault, String type) {
		AccessCardInfo info = handle.getConfig().keycards.guest_keycards.get(type);
		
		if(info != null) {
			long created = Vaults.currentTime();
			
			return new ItemBuilder(Material.PAPER)
					.display("&6" + info.label)
					.lore(createKeycardMeta(vault, created, Vaults.makeTime(info.period)))
					.get();
		}
		
		return null;
	}
	
	public ItemStack resetAndCreateKeycard(UUID vault) {
		VaultData data = handle.getStorage().vaults.get(vault.toString());
		
		if(data != null) {
			long created = Vaults.currentTime();
			
			data.keycard_registered_at = created;
			handle.informUpdate();
			
			return new ItemBuilder(Material.PAPER)
					.display("&6Keycard")
					.lore(createKeycardMeta(vault, created, -1))
					.get();
		}
		
		return null;
	}
}
