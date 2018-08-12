package com.chromaclypse.vaults.data;

import java.util.Map;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;

public class VaultConfig extends ConfigObject {
	public GeneralConfig general = new GeneralConfig();
	
	public static class GeneralConfig {
		public int max_dimension = 15;
	}
	
	public RentalConfig rental = new RentalConfig();
	
	public static class RentalConfig {
		public String period = "1w";
		public double upkeep_ratio = 0.02;
		public double archive_cost = 0.0;
	}
	
	public KeycardConfig keycards = new KeycardConfig();
	
	public static class KeycardConfig {
		public double base_cost = 0.0;
		public double vault_cost_ratio = 0.002;
		
		public Map<String, AccessCardInfo> guest_keycards = Defaults.EmptyMap();
		
		public static class AccessCardInfo {
			public String label = "1-day pass";
			public String period = "1d";
			public double keycard_cost_ratio = 0.1;
		}
	}
	
	public Map<String, VaultInfo> vault_types = Defaults.EmptyMap();
	
	public static class VaultInfo {
		public double cost = 0.0;
		public boolean allows_rentals = true;
		public String door_material = "IRON_BLOCK";
	}
}
