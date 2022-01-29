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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Torrent {

    private final URI announce;
    //private final List<List<URI>> announceUrls;
    private final String createdBy;
    private final Date creationDate;
    private final String comment;
    private final List<TorrentFile> files;
    private final String infoHash;
    private final String name;
    private final Long pieceLength;
    private final List<String> pieces;

    private static final Bencode bencode = new Bencode(true);

    private String sha1(byte[] data) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(data);
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Torrent(final byte[] bytes) throws URISyntaxException {

        final Map<String, Object> data = bencode.decode(bytes, Type.DICTIONARY);

        // Need to handle multiple/tiered announce urls also
        announce = new URI(new String(((ByteBuffer) data.get("announce")).array()));

        createdBy = data.containsKey("created by") ? new String(((ByteBuffer) data.get("created by")).array()) : null;
        creationDate = data.containsKey("creation date") ? new Date((Long) data.get("creation date") * 1000) : null;
        comment = data.containsKey("comment") ? new String(((ByteBuffer) data.get("comment")).array()) : null;

        final Map<String, Object> info = (Map<String, Object>) data.get("info");

        final byte[] infoBytes = bencode.encode(info);
        infoHash = sha1(infoBytes);

        name = info.containsKey("name") ? new String(((ByteBuffer) info.get("name")).array()) : null;
        pieceLength = (Long) info.get("piece length");

        pieces = new LinkedList<>();
        final ByteBuffer piecesBytes = (ByteBuffer) info.get("pieces");
        final int numberOfPieces = piecesBytes.capacity() / 20;
        byte[] pieceBytes = new byte[20];
        for (int x = 1 ; x <= numberOfPieces ; x++ ) {
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

    public URI getAnnounce() {
        return announce;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getComment() {
        return comment;
    }

    public List<TorrentFile> getFiles() {
        return files;
    }

    public String getInfoHash() {
        return infoHash;
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
                ", infoHash='" + infoHash + '\'' +
                ", name='" + name + '\'' +
                ", pieceLength=" + pieceLength +
                '}';
    }
}
