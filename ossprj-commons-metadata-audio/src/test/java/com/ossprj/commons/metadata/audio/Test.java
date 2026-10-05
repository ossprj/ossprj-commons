package com.ossprj.commons.metadata.audio;

import com.ossprj.commons.metadata.audio.flac.function.ExtractMetadataFromFlacFile;
import com.ossprj.commons.metadata.audio.model.TagName;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) {
        final ExtractMetadataFromFlacFile extractMetadataFromFlacFile = new ExtractMetadataFromFlacFile();
        final ExtractUniqueTags extractUniqueTags = new ExtractUniqueTags();

        final Path sourcePath = Paths.get("/media/user/Old/Archive1.Sorted/Ratdog/rd2005-10-28.sbd.instantlivecd.flac16");

        final List<Map<String, String>> sourceTags = Arrays.stream(sourcePath.toFile().listFiles(file -> file.getName().endsWith(".flac")))
                .map(file -> extractMetadataFromFlacFile.apply(file.toPath())).collect(Collectors.toList());

        final Map<String, Set<String>> uniqueTags = extractUniqueTags.apply(sourceTags);

        System.out.println(uniqueTags);

    }
}
