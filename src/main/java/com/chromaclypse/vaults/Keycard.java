package com.chromaclypse.vaults;

import java.util.UUID;

public class Keycard {
	public static final long ETERNAL = -1L;
	
	private final UUID vault;
	private final long createdEpoch;
	private final long expiresEpoch;
	private final boolean isGuest;
	
	public Keycard(UUID vault, long createdEpoch) {
		this.vault = vault;
		this.createdEpoch = createdEpoch;
		this.expiresEpoch = ETERNAL;
		isGuest = false;
	}
	
	public Keycard(UUID vault, long createdEpoch, long expiresEpoch) {
		this.vault = vault;
		this.createdEpoch = createdEpoch;
		this.expiresEpoch = expiresEpoch;
		isGuest = true;
	}
	
	public UUID getVault() {
		return vault;
	}
	
	public long getCreated() {
		return createdEpoch;
	}
	
	public long getExpiry() {
		return expiresEpoch;
	}
	
	public boolean isGuestCard() {
		return isGuest;
	}
	
	public boolean hasExpired() {
		return expiresEpoch != ETERNAL && expiresEpoch < Vaults.currentTime();
	}
}
