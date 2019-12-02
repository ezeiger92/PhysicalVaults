package com.chromaclypse.vaults;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

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
		return expiresEpoch != ETERNAL && expiresEpoch < System.currentTimeMillis();
	}
	
	public static class Serializer implements PersistentDataType<byte[], Keycard> {
		private static final long OWNER_FAKE_EXPIRY = 0;
		
		@Override
		public Class<Keycard> getComplexType() {
			return Keycard.class;
		}

		@Override
		public Class<byte[]> getPrimitiveType() {
			return byte[].class;
		}

		@Override
		public Keycard fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
			ByteBuffer bb = ByteBuffer.wrap(primitive);
			long most = bb.getLong();
			UUID vault = new UUID(most, bb.getLong());
			long created = bb.getLong();
			long expiry = bb.getLong();
			
			if(expiry != OWNER_FAKE_EXPIRY) {
				return new Keycard(vault, created, expiry);
			}
			
			return new Keycard(vault, created);
		}

		@Override
		public byte[] toPrimitive(Keycard complex, PersistentDataAdapterContext context) {
			ByteBuffer bb = ByteBuffer.wrap(new byte[32]);
			UUID first = complex.getVault();
			
			bb.putLong(first.getMostSignificantBits());
			bb.putLong(first.getLeastSignificantBits());
			bb.putLong(complex.getCreated());
			
			if(complex.isGuestCard()) {
				bb.putLong(complex.getExpiry());
			}
			else {
				bb.putLong(OWNER_FAKE_EXPIRY);
			}
			
			return bb.array();
		}
	}
}
