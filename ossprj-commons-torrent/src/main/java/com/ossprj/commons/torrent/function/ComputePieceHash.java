package com.ossprj.commons.torrent.function;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

public class ComputePieceHash implements Callable<byte[]> {

    private final byte[] bytes;

    public ComputePieceHash(byte[] bytes) {
        this.bytes = bytes;
    }

    private byte[] hash(byte[] data) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(data);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] call() throws Exception {
        return hash(bytes);
    }
}
