package com.ossprj.commons.torrent.model;

import java.util.Collections;
import java.util.List;

public class TorrentVerificationReport {

    private final TorrentVerificationStatus status;
    private final List<String> missingPaths;

    public TorrentVerificationReport(TorrentVerificationStatus status) {
        this.status = status;
        this.missingPaths = Collections.emptyList();
    }

    public TorrentVerificationReport(TorrentVerificationStatus status, List<String> missingPaths) {
        this.status = status;
        this.missingPaths = missingPaths;
    }

    public TorrentVerificationStatus getStatus() {
        return status;
    }

    public List<String> getMissingPaths() {
        return missingPaths;
    }

    @Override
    public String toString() {
        return "TorrentVerificationReport{" +
                "status='" + status + '\'' +
                ", missingPaths=" + missingPaths +
                '}';
    }
}
