package com.ossprj.commons.torrent.function;

import com.dampcake.bencode.Bencode;
import com.ossprj.commons.torrent.model.Torrent;
import com.ossprj.commons.torrent.model.TorrentVerificationReport;
import com.ossprj.commons.torrent.model.TorrentVerificationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class VerifyTorrentContentTest {

    private static final Bencode bencode = new Bencode(true);
    private ExecutorService executorService;

    @BeforeEach
    public void setUp() {
        executorService = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    public void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Test
    public void testVerifyMultiFileTorrentWithLeadingZeroHash(@TempDir Path tempDir) throws Exception {
        // Create 2 files in tempDir
        Path dir = tempDir.resolve("content");
        Files.createDirectories(dir);

        byte[] content1 = new byte[30];
        // Crafted to produce leading zero in piece SHA-1
        Arrays.fill(content1, (byte) 'A');
        Path file1 = dir.resolve("file1.bin");
        Files.write(file1, content1);

        byte[] content2 = new byte[70];
        Arrays.fill(content2, (byte) 'B');
        Path file2 = dir.resolve("file2.bin");
        Files.write(file2, content2);

        // Piece length = 50. Total bytes = 100 -> 2 pieces of 50 bytes.
        byte[] piece1Data = new byte[50];
        System.arraycopy(content1, 0, piece1Data, 0, 30);
        System.arraycopy(content2, 0, piece1Data, 30, 20);

        byte[] piece2Data = new byte[50];
        System.arraycopy(content2, 20, piece2Data, 0, 50);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] p1Hash = md.digest(piece1Data);
        md.reset();
        byte[] p2Hash = md.digest(piece2Data);

        byte[] allPieces = new byte[40];
        System.arraycopy(p1Hash, 0, allPieces, 0, 20);
        System.arraycopy(p2Hash, 0, allPieces, 20, 20);

        List<Map<String, Object>> files = new ArrayList<>();
        Map<String, Object> f1Map = new LinkedHashMap<>();
        f1Map.put("length", 30L);
        f1Map.put("path", Collections.singletonList(ByteBuffer.wrap("file1.bin".getBytes(StandardCharsets.UTF_8))));
        files.add(f1Map);

        Map<String, Object> f2Map = new LinkedHashMap<>();
        f2Map.put("length", 70L);
        f2Map.put("path", Collections.singletonList(ByteBuffer.wrap("file2.bin".getBytes(StandardCharsets.UTF_8))));
        files.add(f2Map);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap("content".getBytes(StandardCharsets.UTF_8)));
        info.put("piece length", 50L);
        info.put("pieces", ByteBuffer.wrap(allPieces));
        info.put("files", files);

        Map<String, Object> torrentMap = new LinkedHashMap<>();
        torrentMap.put("info", info);

        byte[] torrentBytes = bencode.encode(torrentMap);
        Torrent torrent = new Torrent(torrentBytes);

        VerifyTorrentContent verifier = new VerifyTorrentContent(executorService);

        // 1. Verify pointing directly to the content directory
        TorrentVerificationReport report1 = verifier.perform(torrent, dir);
        assertEquals(TorrentVerificationStatus.VERIFIED, report1.getStatus());

        // 2. Verify pointing to root tempDir (where 'content' subdirectory resides)
        TorrentVerificationReport report2 = verifier.perform(torrent, tempDir);
        assertEquals(TorrentVerificationStatus.VERIFIED, report2.getStatus());
    }

    @Test
    public void testVerifyMissingFiles(@TempDir Path tempDir) throws Exception {
        byte[] dummyPieces = new byte[20];
        Arrays.fill(dummyPieces, (byte) 0);

        List<Map<String, Object>> files = new ArrayList<>();
        Map<String, Object> f1Map = new LinkedHashMap<>();
        f1Map.put("length", 100L);
        f1Map.put("path", Collections.singletonList(ByteBuffer.wrap("missing.bin".getBytes(StandardCharsets.UTF_8))));
        files.add(f1Map);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap("content".getBytes(StandardCharsets.UTF_8)));
        info.put("piece length", 100L);
        info.put("pieces", ByteBuffer.wrap(dummyPieces));
        info.put("files", files);

        Map<String, Object> torrentMap = new LinkedHashMap<>();
        torrentMap.put("info", info);

        Torrent torrent = new Torrent(bencode.encode(torrentMap));
        VerifyTorrentContent verifier = new VerifyTorrentContent(executorService);

        TorrentVerificationReport report = verifier.perform(torrent, tempDir);
        assertEquals(TorrentVerificationStatus.INCOMPLETE, report.getStatus());
        assertTrue(report.getMissingPaths().contains("missing.bin"));
    }

    @Test
    public void testVerifyCorruptedFiles(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("corrupted.bin");
        Files.write(file, "wrong content".getBytes(StandardCharsets.UTF_8));

        byte[] dummyPieces = new byte[20];
        Arrays.fill(dummyPieces, (byte) 1); // doesn't match hash of "wrong content"

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", ByteBuffer.wrap("corrupted.bin".getBytes(StandardCharsets.UTF_8)));
        info.put("length", (long) "wrong content".getBytes(StandardCharsets.UTF_8).length);
        info.put("piece length", 100L);
        info.put("pieces", ByteBuffer.wrap(dummyPieces));

        Map<String, Object> torrentMap = new LinkedHashMap<>();
        torrentMap.put("info", info);

        Torrent torrent = new Torrent(bencode.encode(torrentMap));
        VerifyTorrentContent verifier = new VerifyTorrentContent(executorService);

        TorrentVerificationReport report = verifier.perform(torrent, tempDir);
        assertEquals(TorrentVerificationStatus.FAILED, report.getStatus());
    }
}
