package com.ossprj.commons.torrent.model;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Torrent {

    // \uFFFD is the character returned when encountering an unprintable UTF-8 character
    private static final String UNPRINTABLE_UTF8_CHARACTER = "�";

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private final URI announce;
    //private final List<List<URI>> announceUrls;
    private final String createdBy;
    private final Long creationDate;
    private final String comment;
    private final List<TorrentFile> files;
    private final byte[] infoHash;
    private final String infoHashHex;
    private final String name;
    private final Long pieceLength;
    private final List<String> pieces;

    private static final Bencode bencode = new Bencode(true);

    private byte[] sha1(byte[] data) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(data);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String getInfoHashAsHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public Torrent(final byte[] bytes) throws URISyntaxException {

        final Map<String, Object> data = bencode.decode(bytes, Type.DICTIONARY);

        // Need to handle multiple/tiered announce urls also
        announce = new URI(new String(((ByteBuffer) data.get("announce")).array()));

        createdBy = data.containsKey("created by") ? new String(((ByteBuffer) data.get("created by")).array()) : null;
        creationDate = data.containsKey("creation date") ? (Long) data.get("creation date") : null;
        comment = data.containsKey("comment") ? new String(((ByteBuffer) data.get("comment")).array()) : null;

        final Map<String, Object> info = (Map<String, Object>) data.get("info");

        final byte[] infoBytes = bencode.encode(info);
        infoHash = sha1(infoBytes);
        infoHashHex = getInfoHashAsHex(infoHash);

        name = info.containsKey("name") ? new String(((ByteBuffer) info.get("name")).array()) : null;
        pieceLength = (Long) info.get("piece length");

        pieces = new LinkedList<>();
        final ByteBuffer piecesBytes = (ByteBuffer) info.get("pieces");
        final int numberOfPieces = piecesBytes.capacity() / 20;
        byte[] pieceBytes = new byte[20];
        for (int x = 1; x <= numberOfPieces; x++) {
            piecesBytes.get(pieceBytes);
            pieces.add(new BigInteger(1, pieceBytes).toString(16));
        }

        // If this is a multi-file torrent extract the individual files
        if (info.containsKey("files")) {
            final List<Map<String, Object>> filesContent = (List<Map<String, Object>>) info.get("files");
            files = filesContent.stream()
                    .map(torrentFile -> {
                        final String path = ((List<ByteBuffer>) torrentFile.get("path")).stream()
                                .map(bb -> new String(bb.array()))
                                .reduce((a, b) -> a + File.separator + b).get();
                        final Long length = Long.valueOf(torrentFile.get("length").toString());
                        return new TorrentFile(path, length);
                    }).collect(Collectors.toList());
        } else {
            // Otherwise just extract the info for the single file
            files = new LinkedList<>();
            files.add(new TorrentFile(name, (Long) info.get("length")));
        }


    }

    public boolean containsZeroLengthFiles() {
        for (final TorrentFile torrentFile : getFiles()) {
            if (torrentFile.getLength() == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean containsUnprintableUTF8CharactersInName() {
        return getName() != null && getName().contains(UNPRINTABLE_UTF8_CHARACTER);
    }

    public boolean containsUnprintableUTF8CharactersInFiles() {
        for (final TorrentFile torrentFile : getFiles()) {
            if (torrentFile.getPath().contains(UNPRINTABLE_UTF8_CHARACTER)) {
                return true;
            }
        }
        return false;
    }

    public URI getAnnounce() {
        return announce;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public String getComment() {
        return comment;
    }

    public List<TorrentFile> getFiles() {
        return files;
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public String getInfoHashHex() {
        return infoHashHex;
    }

    public String getName() {
        return name;
    }

    public Long getPieceLength() {
        return pieceLength;
    }

    public List<String> getPieces() {
        return pieces;
    }

    @Override
    public String toString() {
        return "Torrent{" +
                "announce=" + announce +
                ", createdBy='" + createdBy + '\'' +
                ", creationDate=" + creationDate +
                ", comment='" + comment + '\'' +
                ", files=" + files +
                ", infoHashHex='" + infoHashHex + '\'' +
                ", name='" + name + '\'' +
                ", pieceLength=" + pieceLength +
                '}';
    }
}
