package com.ossprj.commons.torrent.function;

import com.ossprj.commons.torrent.model.Torrent;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

public class CalculateTorrentContentHash implements Function<Torrent, String> {

    public String apply(final Torrent torrent) {

        if (torrent.getFiles() == null || torrent.getFiles().isEmpty()) {
            throw new IllegalArgumentException("torrent must contain at least one file path");
        }

        final String concatenatedPaths = torrent.getFiles().stream()
                // ??? Filter out zero length files
                // .filter(torrentFile -> torrentFile.getLength() == 0)
                .map(torrentFile -> torrentFile.getPath() + torrentFile.getLength())
                // Pull out the OS specific file separator character
                .map(torrentFile -> torrentFile.replace(File.separator, "").replace("/", "").replace("\\", ""))
                .map(String::toLowerCase)
                .sorted()
                .reduce((a, b) -> a + b).orElse("");

        return md5(concatenatedPaths.getBytes(StandardCharsets.UTF_8));
    }

    private String md5(byte[] data) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data);
            byte[] digest = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
