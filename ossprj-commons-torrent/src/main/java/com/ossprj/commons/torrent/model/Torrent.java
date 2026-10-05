package com.ossprj.commons.torrent.model;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
    private final List<List<URI>> announceList;
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

    private static String getInfoHashAsHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static String byteBufferToString(ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        ByteBuffer duplicate = buffer.duplicate();
        byte[] bytes = new byte[duplicate.remaining()];
        duplicate.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] extractRawInfoBytes(final byte[] data) {
        if (data == null || data.length == 0 || data[0] != 'd') {
            return null;
        }
        try {
            int pos = 1;
            while (pos < data.length && data[pos] != 'e') {
                int colon = pos;
                while (colon < data.length && data[colon] != ':') {
                    colon++;
                }
                if (colon >= data.length) break;
                int keyLen = Integer.parseInt(new String(data, pos, colon - pos, StandardCharsets.US_ASCII));
                int keyStart = colon + 1;
                int keyEnd = keyStart + keyLen;
                String key = new String(data, keyStart, keyLen, StandardCharsets.ISO_8859_1);

                int valEnd = skipBencodedElement(data, keyEnd);

                if ("info".equals(key)) {
                    byte[] infoBytes = new byte[valEnd - keyEnd];
                    System.arraycopy(data, keyEnd, infoBytes, 0, infoBytes.length);
                    return infoBytes;
                }
                pos = valEnd;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int skipBencodedElement(final byte[] data, int pos) {
        if (pos >= data.length) return data.length;
        byte b = data[pos];
        if (b == 'i') {
            int end = pos + 1;
            while (end < data.length && data[end] != 'e') end++;
            return end + 1;
        } else if (b == 'l') {
            int cur = pos + 1;
            while (cur < data.length && data[cur] != 'e') {
                cur = skipBencodedElement(data, cur);
            }
            return cur + 1;
        } else if (b == 'd') {
            int cur = pos + 1;
            while (cur < data.length && data[cur] != 'e') {
                cur = skipBencodedElement(data, cur); // key
                cur = skipBencodedElement(data, cur); // value
            }
            return cur + 1;
        } else if (b >= '0' && b <= '9') {
            int colon = pos;
            while (colon < data.length && data[colon] != ':') colon++;
            int len = Integer.parseInt(new String(data, pos, colon - pos, StandardCharsets.US_ASCII));
            return colon + 1 + len;
        }
        throw new IllegalArgumentException("Invalid bencode token at " + pos);
    }

    public Torrent(final byte[] bytes) throws URISyntaxException {

        final Map<String, Object> data = bencode.decode(bytes, Type.DICTIONARY);

        URI parsedAnnounce = null;
        if (data.containsKey("announce") && data.get("announce") != null) {
            String announceStr = byteBufferToString((ByteBuffer) data.get("announce"));
            if (announceStr != null && !announceStr.trim().isEmpty()) {
                parsedAnnounce = new URI(announceStr);
            }
        }
        announce = parsedAnnounce;

        List<List<URI>> parsedAnnounceList = new LinkedList<>();
        if (data.containsKey("announce-list") && data.get("announce-list") != null) {
            try {
                List<List<?>> tiers = (List<List<?>>) data.get("announce-list");
                for (List<?> tier : tiers) {
                    List<URI> tierUrls = new LinkedList<>();
                    for (Object item : tier) {
                        if (item instanceof ByteBuffer) {
                            String uriStr = byteBufferToString((ByteBuffer) item);
                            if (uriStr != null && !uriStr.trim().isEmpty()) {
                                tierUrls.add(new URI(uriStr));
                            }
                        }
                    }
                    if (!tierUrls.isEmpty()) {
                        parsedAnnounceList.add(tierUrls);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        announceList = parsedAnnounceList;

        createdBy = data.containsKey("created by") ? byteBufferToString((ByteBuffer) data.get("created by")) : null;
        creationDate = data.containsKey("creation date") ? (Long) data.get("creation date") : null;
        comment = data.containsKey("comment") ? byteBufferToString((ByteBuffer) data.get("comment")) : null;

        final Map<String, Object> info = (Map<String, Object>) data.get("info");

        byte[] rawInfo = extractRawInfoBytes(bytes);
        if (rawInfo == null) {
            rawInfo = bencode.encode(info);
        }
        infoHash = sha1(rawInfo);
        infoHashHex = getInfoHashAsHex(infoHash);

        name = info.containsKey("name") ? byteBufferToString((ByteBuffer) info.get("name")) : null;
        pieceLength = info.containsKey("piece length") && info.get("piece length") != null ? (Long) info.get("piece length") : null;

        pieces = new LinkedList<>();
        if (info.containsKey("pieces") && info.get("pieces") != null) {
            final ByteBuffer piecesBytes = (ByteBuffer) info.get("pieces");
            final int numberOfPieces = piecesBytes.remaining() / 20;
            byte[] pieceBytes = new byte[20];
            for (int x = 1; x <= numberOfPieces; x++) {
                piecesBytes.get(pieceBytes);
                pieces.add(getInfoHashAsHex(pieceBytes));
            }
        }

        // If this is a multi-file torrent extract the individual files
        if (info.containsKey("files")) {
            final List<Map<String, Object>> filesContent = (List<Map<String, Object>>) info.get("files");
            files = filesContent.stream()
                    .map(torrentFile -> {
                        final String path = ((List<ByteBuffer>) torrentFile.get("path")).stream()
                                .map(Torrent::byteBufferToString)
                                .reduce((a, b) -> a + File.separator + b).orElse("");
                        final Long length = Long.valueOf(torrentFile.get("length").toString());
                        return new TorrentFile(path, length);
                    }).collect(Collectors.toList());
        } else {
            // Otherwise just extract the info for the single file
            files = new LinkedList<>();
            Long length = info.containsKey("length") && info.get("length") != null ? (Long) info.get("length") : null;
            files.add(new TorrentFile(name, length));
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

    public List<List<URI>> getAnnounceList() {
        return announceList;
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
                ", announceList=" + announceList +
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
