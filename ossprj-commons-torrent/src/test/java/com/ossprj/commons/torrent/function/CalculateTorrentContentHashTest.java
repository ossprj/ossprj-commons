package com.ossprj.commons.torrent.function;

import com.dampcake.bencode.Bencode;
import com.ossprj.commons.torrent.model.Torrent;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CalculateTorrentContentHashTest {

    private static final Bencode bencode = new Bencode(true);

    @Test
    public void testContentHashCrossPlatformSeparatorHandling() throws Exception {
        byte[] pieceBytes = new byte[20];
        Arrays.fill(pieceBytes, (byte) 1);

        List<Map<String, Object>> files = new ArrayList<>();

        Map<String, Object> file1 = new LinkedHashMap<>();
        file1.put("length", 100L);
        file1.put("path", Arrays.asList(
                ByteBuffer.wrap("folder".getBytes(StandardCharsets.UTF_8)),
                ByteBuffer.wrap("file1.txt".getBytes(StandardCharsets.UTF_8))
        ));
        files.add(file1);

        Map<String, Object> file2 = new LinkedHashMap<>();
        file2.put("length", 200L);
        file2.put("path", Collections.singletonList(
                ByteBuffer.wrap("file2.txt".getBytes(StandardCharsets.UTF_8))
        ));
        files.add(file2);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap("TestDir".getBytes(StandardCharsets.UTF_8)));
        info.put("piece length", 1000L);
        info.put("pieces", ByteBuffer.wrap(pieceBytes));
        info.put("files", files);

        Map<String, Object> torrentMap = new LinkedHashMap<>();
        torrentMap.put("info", info);

        byte[] torrentBytes = bencode.encode(torrentMap);
        Torrent torrent = new Torrent(torrentBytes);

        CalculateTorrentContentHash contentHashFunc = new CalculateTorrentContentHash();
        String hash = contentHashFunc.apply(torrent);

        assertNotNull(hash);
        assertEquals(32, hash.length());
        // Verify MD5 is hex string
        assertTrue(hash.matches("^[0-9a-f]{32}$"));
    }

    @Test
    public void testThrowsOnEmptyFiles() throws Exception {
        byte[] pieceBytes = new byte[20];
        Arrays.fill(pieceBytes, (byte) 1);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap("TestDir".getBytes(StandardCharsets.UTF_8)));
        info.put("piece length", 1000L);
        info.put("pieces", ByteBuffer.wrap(pieceBytes));
        info.put("files", Collections.emptyList());

        Map<String, Object> torrentMap = new LinkedHashMap<>();
        torrentMap.put("info", info);

        byte[] torrentBytes = bencode.encode(torrentMap);
        Torrent torrent = new Torrent(torrentBytes);

        CalculateTorrentContentHash contentHashFunc = new CalculateTorrentContentHash();
        assertThrows(IllegalArgumentException.class, () -> contentHashFunc.apply(torrent));
    }
}
