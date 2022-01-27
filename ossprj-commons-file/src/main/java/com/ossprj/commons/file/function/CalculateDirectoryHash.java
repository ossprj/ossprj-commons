package com.ossprj.commons.file.function;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calculates a "directory hash" which can be used to quickly compare the contents of two directories
 *
 * Finds all files recursively in a directory, relativizes them (and appends the file length to that relative path),
 * lower cases them, sorts them, concatenates them into a string and then MD5 hashes that string.
 *
 * NOTE:
 * This hash does NOT take into account the contents of the files just the contents of the directory.
 * Two directories with an identical set of files with identical lengths but different file contents will hash the same.
 *
 * This hash does NOT take into account different file separators across operating systems.
 * Two hashes from file systems with differing file separators (i.e. Linux/MacOs vs Windows) may not produce the same hash.
 */
public class CalculateDirectoryHash implements Function<Path,String> {

    public String apply(final Path directory) {
        if (!directory.toFile().isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }

        // Find all files in the directory, ignore directories
        final List<Path> files;
        try {
            files = Files.find(directory, Integer.MAX_VALUE, (path, attributes) -> !path.toFile().isDirectory())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (files.isEmpty()) {
            throw new IllegalArgumentException(directory + " must contain at least one file");
        }

        // Concatenate all the info into one long string
        final String concatenatedPaths = files.stream()
                .map(directory::relativize)
                .map(path -> path.toString() + directory.resolve(path).toFile().length())
                .map(String::toLowerCase)
                .sorted()
                .reduce((a, b) -> a + b).get();

        // Hash it
        return DigestUtils.md5Hex(concatenatedPaths).toUpperCase();
    }

}
