package com.ossprj.commons.torrent.model;

import java.util.Collections;
import java.util.List;

public class TorrentValidationReport {

    private final TorrentValidationStatus status;
    private final List<String> missingPaths;

    public TorrentValidationReport(TorrentValidationStatus status) {
        this.status = status;
        this.missingPaths = Collections.emptyList();
    }

    public TorrentValidationReport(TorrentValidationStatus status, List<String> missingPaths) {
        this.status = status;
        this.missingPaths = missingPaths;
    }

    public TorrentValidationStatus getStatus() {
        return status;
    }

    public List<String> getMissingPaths() {
        return missingPaths;
    }

    @Override
    public String toString() {
        return "TorrentValidationReport{" +
                "status='" + status + '\'' +
                ", missingPaths=" + missingPaths +
                '}';
    }
}
