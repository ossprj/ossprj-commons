package com.ossprj.commons.torrent.function;

import com.ossprj.commons.torrent.model.Torrent;
import com.ossprj.commons.torrent.model.TorrentFile;
import com.ossprj.commons.torrent.model.TorrentVerificationReport;
import com.ossprj.commons.torrent.model.TorrentVerificationStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class VerifyTorrentContent {

    private final ExecutorService executorService;

    public VerifyTorrentContent(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public TorrentVerificationReport perform(final Torrent torrent, final Path torrentPath) throws InterruptedException, ExecutionException, IOException {

        // Validate all the files (with lengths > 0) exist. If some files are missing the torrent won't validate
        final List<String> missingPaths = new LinkedList<>();
        for (final TorrentFile torrentFile : torrent.getFiles()) {
            // Ignore non-zero length files
            if (torrentFile.getLength() > 0) {
                final Path torrentFilePath = torrentPath.resolve(torrentFile.getPath());
                // If the file is missing add it to the list
                if (!torrentFilePath.toFile().exists()) {
                    missingPaths.add(torrentFile.getPath());
                }
            }
        }
        if (!missingPaths.isEmpty()) {
            return new TorrentVerificationReport(TorrentVerificationStatus.INCOMPLETE, missingPaths);
        }

        final List<byte[]> hashes = getHashes(torrentPath,torrent.getFiles(),torrent.getPieceLength().intValue());

        final String piecesHashes = hashes.stream()
                .map(bytes -> new BigInteger(1, bytes).toString(16))
                .reduce((a, b) -> a + b).get();
        //System.out.println("piecesHashes: " + piecesHashes);

        final String torrentPiecesHashes = torrent.getPieces().stream()
                .reduce((a, b) -> a + b).get();
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

    private List<byte[]> getHashes(final Path torrentPath, final List<TorrentFile> torrentFiles, final Integer pieceLength) throws IOException, ExecutionException, InterruptedException {

        final List<byte[]> hashes = new LinkedList<>();
        final List<Future<byte[]>> futures = new LinkedList<>();

        byte[] pieceBuffer = new byte[pieceLength];
        int totalBytesRead = 0;

        for (TorrentFile torrentFile : torrentFiles) {

            // Ignore empty files
            if (torrentFile.getLength() > 0) {
                //System.out.println("Processing: " + torrentFile.getPath());
                try (FileInputStream fis = new FileInputStream(torrentPath.resolve(torrentFile.getPath()).toFile())) {
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
