package com.ossprj.commons.torrent.model;

public class TorrentFile {

    private final String path;
    private final Long length;

    public TorrentFile(String path, Long length) {
        this.path = path;
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public Long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "TorrentFile{" +
                "path='" + path + '\'' +
                ", length=" + length +
                '}';
    }
}
