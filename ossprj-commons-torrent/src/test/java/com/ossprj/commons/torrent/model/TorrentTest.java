package com.ossprj.commons.torrent.model;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TorrentTest {

    private static final Bencode bencode = new Bencode(true);

    private byte[] createTorrentBytes(Map<String, Object> torrentMap) {
        return bencode.encode(torrentMap);
    }

    @Test
    public void testPieceHashLeadingZerosPreserved() throws Exception {
        // Create 2 pieces: one starting with 0x00 (which previously caused 38-39 chars), one normal
        byte[] piece1 = new byte[20];
        piece1[0] = 0x00;
        piece1[1] = 0x01;
        piece1[19] = (byte) 0xfe;

        byte[] piece2 = new byte[20];
        Arrays.fill(piece2, (byte) 0x0a);

        byte[] allPieces = new byte[40];
        System.arraycopy(piece1, 0, allPieces, 0, 20);
        System.arraycopy(piece2, 0, allPieces, 20, 20);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap("sample.txt".getBytes(StandardCharsets.UTF_8)));
        info.put("length", 100L);
        info.put("piece length", 50L);
        info.put("pieces", ByteBuffer.wrap(allPieces));

        Map<String, Object> torrentMap = new LinkedHashMap<>();
        torrentMap.put("announce", ByteBuffer.wrap("http://tracker.example.com/announce".getBytes(StandardCharsets.UTF_8)));
        torrentMap.put("info", info);

        byte[] torrentBytes = createTorrentBytes(torrentMap);
        Torrent torrent = new Torrent(torrentBytes);

        List<String> pieces = torrent.getPieces();
        assertEquals(2, pieces.size());
        assertEquals(40, pieces.get(0).length());
        assertTrue(pieces.get(0).startsWith("0001"));
        assertEquals(40, pieces.get(1).length());
        assertEquals("0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a", pieces.get(1));
    }

    @Test
    public void testRawInfoHashParity() throws Exception {
        // Construct raw bencoded torrent bytes manually with custom key order
        String rawTorrent = "d8:announce27:http://example.com/announce4:infod6:lengthi1234e4:name8:test.txtee";
        byte[] torrentBytes = rawTorrent.getBytes(StandardCharsets.ISO_8859_1);

        Torrent torrent = new Torrent(torrentBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        String rawInfoStr = "d6:lengthi1234e4:name8:test.txte";
        byte[] expectedInfoHash = md.digest(rawInfoStr.getBytes(StandardCharsets.ISO_8859_1));

        StringBuilder sb = new StringBuilder();
        for (byte b : expectedInfoHash) {
            sb.append(String.format("%02x", b));
        }

        assertArrayEquals(expectedInfoHash, torrent.getInfoHash());
        assertEquals(sb.toString(), torrent.getInfoHashHex());
    }

    @Test
    public void testUtf8EncodingAndByteBufferSlicing() throws Exception {
        String unicodeName = "日本語_файл_café_🚀";
        String comment = "Torrent commentary with accents: éàç";
        String createdBy = "Client/1.0.0 (ünicöde)";

        byte[] pieceBytes = new byte[20];
        Arrays.fill(pieceBytes, (byte) 1);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap(unicodeName.getBytes(StandardCharsets.UTF_8)));
        info.put("length", 1024L);
        info.put("piece length", 1024L);
        info.put("pieces", ByteBuffer.wrap(pieceBytes));

        Map<String, Object> torrentMap = new LinkedHashMap<>();
        torrentMap.put("announce", ByteBuffer.wrap("http://example.com/announce".getBytes(StandardCharsets.UTF_8)));
        torrentMap.put("comment", ByteBuffer.wrap(comment.getBytes(StandardCharsets.UTF_8)));
        torrentMap.put("created by", ByteBuffer.wrap(createdBy.getBytes(StandardCharsets.UTF_8)));
        torrentMap.put("creation date", 1700000000L);
        torrentMap.put("info", info);

        byte[] torrentBytes = createTorrentBytes(torrentMap);
        Torrent torrent = new Torrent(torrentBytes);

        assertEquals(unicodeName, torrent.getName());
        assertEquals(comment, torrent.getComment());
        assertEquals(createdBy, torrent.getCreatedBy());
        assertEquals(Long.valueOf(1700000000L), torrent.getCreationDate());
        assertEquals(1, torrent.getFiles().size());
        assertEquals(unicodeName, torrent.getFiles().get(0).getPath());
    }

    @Test
    public void testTrackerlessAndMultiTrackerSupport() throws Exception {
        byte[] pieceBytes = new byte[20];
        Arrays.fill(pieceBytes, (byte) 1);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap("trackerless.iso".getBytes(StandardCharsets.UTF_8)));
        info.put("length", 1024L);
        info.put("piece length", 1024L);
        info.put("pieces", ByteBuffer.wrap(pieceBytes));

        // 1. Trackerless (no announce key)
        Map<String, Object> trackerlessMap = new LinkedHashMap<>();
        trackerlessMap.put("info", info);
        byte[] trackerlessBytes = createTorrentBytes(trackerlessMap);

        Torrent trackerlessTorrent = new Torrent(trackerlessBytes);
        assertNull(trackerlessTorrent.getAnnounce());
        assertTrue(trackerlessTorrent.getAnnounceList().isEmpty());

        // 2. Multi-tracker with announce-list (BEP 0012)
        List<List<ByteBuffer>> announceList = new ArrayList<>();
        announceList.add(Arrays.asList(
                ByteBuffer.wrap("http://tracker1.tier1.com/announce".getBytes(StandardCharsets.UTF_8)),
                ByteBuffer.wrap("http://tracker2.tier1.com/announce".getBytes(StandardCharsets.UTF_8))
        ));
        announceList.add(Arrays.asList(
                ByteBuffer.wrap("udp://tracker1.tier2.com:1337/announce".getBytes(StandardCharsets.UTF_8))
        ));

        Map<String, Object> multiTrackerMap = new LinkedHashMap<>();
        multiTrackerMap.put("announce", ByteBuffer.wrap("http://default.tracker.com/announce".getBytes(StandardCharsets.UTF_8)));
        multiTrackerMap.put("announce-list", announceList);
        multiTrackerMap.put("info", info);

        byte[] multiTrackerBytes = createTorrentBytes(multiTrackerMap);
        Torrent multiTrackerTorrent = new Torrent(multiTrackerBytes);

        assertEquals(new URI("http://default.tracker.com/announce"), multiTrackerTorrent.getAnnounce());
        assertEquals(2, multiTrackerTorrent.getAnnounceList().size());
        assertEquals(2, multiTrackerTorrent.getAnnounceList().get(0).size());
        assertEquals(new URI("http://tracker1.tier1.com/announce"), multiTrackerTorrent.getAnnounceList().get(0).get(0));
        assertEquals(new URI("http://tracker2.tier1.com/announce"), multiTrackerTorrent.getAnnounceList().get(0).get(1));
        assertEquals(1, multiTrackerTorrent.getAnnounceList().get(1).size());
        assertEquals(new URI("udp://tracker1.tier2.com:1337/announce"), multiTrackerTorrent.getAnnounceList().get(1).get(0));
    }

    @Test
    public void testMultiFileTorrentParsing() throws Exception {
        byte[] pieceBytes = new byte[20];
        Arrays.fill(pieceBytes, (byte) 2);

        List<Map<String, Object>> files = new ArrayList<>();

        Map<String, Object> file1 = new LinkedHashMap<>();
        file1.put("length", 500L);
        file1.put("path", Arrays.asList(
                ByteBuffer.wrap("subfolder".getBytes(StandardCharsets.UTF_8)),
                ByteBuffer.wrap("file1.txt".getBytes(StandardCharsets.UTF_8))
        ));
        files.add(file1);

        Map<String, Object> file2 = new LinkedHashMap<>();
        file2.put("length", 1500L);
        file2.put("path", Collections.singletonList(
                ByteBuffer.wrap("file2.jpg".getBytes(StandardCharsets.UTF_8))
        ));
        files.add(file2);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap("MyCollection".getBytes(StandardCharsets.UTF_8)));
        info.put("piece length", 2000L);
        info.put("pieces", ByteBuffer.wrap(pieceBytes));
        info.put("files", files);

        Map<String, Object> torrentMap = new LinkedHashMap<>();
        torrentMap.put("info", info);

        byte[] torrentBytes = createTorrentBytes(torrentMap);
        Torrent torrent = new Torrent(torrentBytes);

        assertEquals("MyCollection", torrent.getName());
        assertEquals(2, torrent.getFiles().size());
        assertEquals("subfolder" + java.io.File.separator + "file1.txt", torrent.getFiles().get(0).getPath());
        assertEquals(Long.valueOf(500L), torrent.getFiles().get(0).getLength());
        assertEquals("file2.jpg", torrent.getFiles().get(1).getPath());
        assertEquals(Long.valueOf(1500L), torrent.getFiles().get(1).getLength());
    }
}
