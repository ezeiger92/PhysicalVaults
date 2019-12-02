package com.chromaclypse.vaults;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.chromaclypse.api.annotation.Nullable;
import com.chromaclypse.api.item.ItemBuilder;
import com.chromaclypse.vaults.data.VaultConfig.KeycardConfig.AccessCardInfo;
import com.chromaclypse.vaults.data.VaultStorage.VaultData;

public class KeycardFactory {
	private final NamespacedKey KEYCARD_DATA = new NamespacedKey(JavaPlugin.getPlugin(VaultsMain.class), "keycard");
	private final Keycard.Serializer SERIALIZER = new Keycard.Serializer();
	private final Vaults handle;
	private final SimpleDateFormat format = new SimpleDateFormat("mm/dd/yyyy");
	
	public KeycardFactory(Vaults handle) {
		this.handle = handle;
	}
	
	public Keycard GetKeycard(ItemStack keycard) {
		if(keycard == null || keycard.getType() == Material.AIR) {
			return null;
		}
		
		ItemMeta meta = keycard.getItemMeta();
		
		return meta.getPersistentDataContainer().get(KEYCARD_DATA, SERIALIZER);
	}
	
	public UUID getVaultAccess(ItemStack keycard) {
		Keycard card = GetKeycard(keycard);
		
		if(card != null) {
			return card.getVault();
		}
		
		return null;
	}
	
	public ItemStack createGuestKeycard(UUID vault, String type) {
		AccessCardInfo info = handle.getConfig().keycards.guest_keycards.get(type);
		
		if(info != null) {
			long created = System.currentTimeMillis();
			Keycard card = new Keycard(vault, created, created + Vaults.makeTime(info.period));
			ItemStack result = new ItemStack(Material.NAME_TAG);
			
			updateKeycard(result, card);
			
			return result;
		}
		
		return null;
	}
	
	public ItemStack resetAndCreateKeycard(UUID vault) {
		VaultData data = handle.getStorage().vaults.get(vault.toString());
		
		if(data != null) {
			long created = System.currentTimeMillis();
			Keycard card = new Keycard(vault, created);
			ItemStack result = new ItemStack(Material.NAME_TAG);
			
			updateKeycard(result, card, data);
			
			data.keycard_registered_at = created;
			handle.informUpdate();
			
			return result;
		}
		
		return null;
	}
	
	public void updateKeycard(ItemStack stack, @Nullable Keycard card) {
		updateKeycard(stack, card, card != null ? handle.getStorage().vaults.get(card.getVault().toString()) : null);
	}
	
	private void updateKeycard(ItemStack stack, @Nullable Keycard card, VaultData data) {
		if(stack == null || stack.getType() == Material.AIR) {
			return;
		}
		
		if(card == null) {
			ItemBuilder.edit(stack).lore(" &7&l-VOID-");
			if(stack.hasItemMeta()) {
				ItemMeta meta = stack.getItemMeta();
				meta.getPersistentDataContainer().remove(KEYCARD_DATA);
				stack.setItemMeta(meta);
			}
			return;
		}
		
		String lore = " &7Vault: &f" + data.vault_type + "\\n &7Access: &f";
		
		if(card.isGuestCard()) {
			lore += "Temporary\\n &7Expires: &f" + format.format(new Date(card.getExpiry()));
		}
		else lore += "Permanent";
		
		ItemBuilder.edit(stack)
			.display("&dVault Keycard")
			.wrapLore(lore)
			.forceEnchant(Enchantment.DURABILITY, 1)
			.flag(ItemFlag.HIDE_ENCHANTS);
		
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(KEYCARD_DATA, SERIALIZER, card);
		stack.setItemMeta(meta);
	}
}
