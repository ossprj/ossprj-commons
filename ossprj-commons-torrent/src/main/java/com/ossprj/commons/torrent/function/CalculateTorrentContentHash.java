package com.ossprj.commons.torrent.function;

import com.ossprj.commons.torrent.model.Torrent;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

public class CalculateTorrentContentHash implements Function<Torrent, String> {

    public String apply(final Torrent torrent) {

        if (torrent.getFiles() == null || torrent.getFiles().isEmpty()) {
            throw new IllegalArgumentException("torrent must contain at least one file path");
        }

        final String concatenatedPaths = torrent.getFiles().stream()
                .map(torrentFile -> torrentFile.getPath() + torrentFile.getLength())
                // Pull out the OS specific file separator character
                .map(torrentFile -> torrentFile.replaceAll(File.separator, ""))
                .map(String::toLowerCase)
                .sorted()
                .reduce((a, b) -> a + b).get();

        return md5(concatenatedPaths.getBytes());
    }

    private String md5(byte[] data) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data);
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
