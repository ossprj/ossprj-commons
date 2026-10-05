package com.ossprj.commons.file;

import com.ossprj.commons.file.function.CalculateDirectoryContentHash;

import java.nio.file.Paths;

public class CalculateDirectoryContentHashTest {

    public static void main(String[] args) {
        System.out.println(new CalculateDirectoryContentHash().apply(Paths.get("/raid1/Torrent/LosslessLegs.Complete/wsp1999-05-02.028346.sbd.flac16")));
    }
}
