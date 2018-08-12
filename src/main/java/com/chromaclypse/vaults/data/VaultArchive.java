package com.chromaclypse.vaults.data;

import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;
import com.chromaclypse.api.config.Section;

@Section(path="archive_items.yml")
public class VaultArchive extends ConfigObject {

	public Map<String, ArchiveData> archive = Defaults.EmptyMap();
	
	public static class ArchiveData {
		public long archived_at = 0;
		public List<ItemStack> contents = Defaults.EmptyList();
	}
}
