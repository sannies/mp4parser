package org.mp4parser.muxer.tracks.encryption;

import javax.crypto.SecretKey;
import java.util.UUID;

/**
 * Pairs up KeyId with Key.
 */
public class KeyIdKeyPair {
    private SecretKey key;
    private UUID keyId;

    public KeyIdKeyPair( UUID keyId, SecretKey key) {
        this.key = key;
        this.keyId = keyId;
    }

    public SecretKey getKey() {
        return key;
    }

    public UUID getKeyId() {
        return keyId;
    }
}
