package com.ossprj.commons.torrent.function;

import com.ossprj.commons.torrent.model.Torrent;
import com.ossprj.commons.torrent.model.TorrentFile;
import com.ossprj.commons.torrent.model.TorrentVerificationReport;
import com.ossprj.commons.torrent.model.TorrentVerificationStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class VerifyTorrentContent {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private final ExecutorService executorService;

    public VerifyTorrentContent(ExecutorService executorService) {
        this.executorService = executorService;
    }

    private static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private Path resolvePath(final Path basePath, final Torrent torrent, final TorrentFile torrentFile) {
        Path direct = basePath.resolve(torrentFile.getPath());
        if (direct.toFile().exists()) {
            return direct;
        }
        if (torrent.getName() != null && !torrent.getName().isEmpty()) {
            Path nested = basePath.resolve(torrent.getName()).resolve(torrentFile.getPath());
            if (nested.toFile().exists()) {
                return nested;
            }
        }
        return direct;
    }

    public TorrentVerificationReport perform(final Torrent torrent, final Path torrentPath) throws InterruptedException, ExecutionException, IOException {

        // Validate all the files (with lengths > 0) exist. If some files are missing the torrent won't validate
        final List<String> missingPaths = new LinkedList<>();
        for (final TorrentFile torrentFile : torrent.getFiles()) {
            // Ignore non-zero length files
            if (torrentFile.getLength() > 0) {
                final Path torrentFilePath = resolvePath(torrentPath, torrent, torrentFile);
                // If the file is missing add it to the list
                if (!torrentFilePath.toFile().exists()) {
                    missingPaths.add(torrentFile.getPath());
                }
            }
        }
        if (!missingPaths.isEmpty()) {
            return new TorrentVerificationReport(TorrentVerificationStatus.INCOMPLETE, missingPaths);
        }

        final List<byte[]> hashes = getHashes(torrentPath, torrent, torrent.getPieceLength().intValue());

        final String piecesHashes = hashes.stream()
                .map(VerifyTorrentContent::toHex)
                .reduce((a, b) -> a + b).orElse("");
        //System.out.println("piecesHashes: " + piecesHashes);

        final String torrentPiecesHashes = torrent.getPieces().stream()
                .reduce((a, b) -> a + b).orElse("");
        //System.out.println("torrentPiecesHashes: " + torrentPiecesHashes);

        final boolean verified = piecesHashes.equals(torrentPiecesHashes);
        //System.out.println("verified: " + verified);

        return new TorrentVerificationReport(verified ? TorrentVerificationStatus.VERIFIED : TorrentVerificationStatus.FAILED);
    }

    private void processHashes(final List<Future<byte[]>> futures, final List<byte[]> hashes) throws InterruptedException, ExecutionException {
        while (!futures.isEmpty()) {
            hashes.add(futures.remove(0).get());
        }
    }

    private List<byte[]> getHashes(final Path torrentPath, final Torrent torrent, final Integer pieceLength) throws IOException, ExecutionException, InterruptedException {

        final List<byte[]> hashes = new LinkedList<>();
        final List<Future<byte[]>> futures = new LinkedList<>();

        byte[] pieceBuffer = new byte[pieceLength];
        int totalBytesRead = 0;

        for (TorrentFile torrentFile : torrent.getFiles()) {

            // Ignore empty files
            if (torrentFile.getLength() > 0) {
                //System.out.println("Processing: " + torrentFile.getPath());
                Path filePath = resolvePath(torrentPath, torrent, torrentFile);
                try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                    while (true) {

                        // Read up to the number of bytes we need to fill out the current pieceBuffer
                        int readFromStream = fis.read(pieceBuffer, totalBytesRead, pieceBuffer.length - totalBytesRead);

                        // If we are out of bytes to read from this stream, move on to the next
                        if (readFromStream < 0) {
                            break;
                        }

                        totalBytesRead += readFromStream;
                        // If we've filled up the buffer send it off to be hashed
                        if (totalBytesRead == pieceBuffer.length) {
                            futures.add(executorService.submit(new ComputePieceHash(Arrays.copyOf(pieceBuffer, pieceBuffer.length))));
                            processHashes(futures, hashes);
                            totalBytesRead = 0;
                        }
                    }
                }
            }
        }

        // If there are any bytes left after the last file submit those as the last piece
        if (totalBytesRead > 0) {
            // Do we really need to copy this particular buffer ? The buffer wont be reused since its the last one so...
            futures.add(executorService.submit(new ComputePieceHash(Arrays.copyOf(pieceBuffer, totalBytesRead))));
        }

        // Process any remaining futures
        processHashes(futures, hashes);

        return hashes;
    }

}
