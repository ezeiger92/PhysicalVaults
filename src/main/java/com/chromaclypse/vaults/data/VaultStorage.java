package com.chromaclypse.vaults.data;

import java.util.Map;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;
import com.chromaclypse.api.config.Section;

@Section(path="vault_data.yml")
public class VaultStorage extends ConfigObject {

	public long last_upkeep = 0;
	
	public Map<String, VaultData> vaults = Defaults.EmptyMap();
	
	public static class VaultData {
		public String vault_type = "";
		public String world_name = "";
		public Point minimum = new Point();
		public Point maximum = new Point();
		public Point force_exit = new Point();
		public String owner_uuid = "";
		public boolean is_rental = true;
		public long first_payed_at = 0;
		public long last_payed_at = 0;
		public long keycard_registered_at = 0;
	}
	
	public static class Point {
		public int x = 0;
		public int y = 0;
		public int z = 0;
	}
 }
