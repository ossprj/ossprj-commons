package com.ossprj.commons.file.function;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calculates a "directory content hash" which can be used to quickly compare the contents of two directories
 * <p>
 * Find all file paths recursively in a directory (ignores empty directories), relative to the parent directory and
 * append the file length to that relative path, removes OS specific file separators, lower cases them, sorts them,
 * concatenates them into a string and then MD5 hashes that string.
 * <p>
 * NOTE:
 * This hash does NOT take into account the contents of the files just the contents of the directory.
 * Two directories with an identical set of files with identical lengths but different file contents will hash the same.
 */
public class CalculateDirectoryContentHash implements Function<Path, String> {

    public String apply(final Path directory) {
        if (!directory.toFile().isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }

        // Find all files in the directory, ignore directories
        final List<Path> files;
        try {
            files = Files.find(directory, Integer.MAX_VALUE, (path, attributes) -> !path.toFile().isDirectory(), FileVisitOption.FOLLOW_LINKS)
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
                // Ensure we remove the OS specific file separator so hashes match across different OS
                .map(path -> path.toString().replaceAll(File.separator, "") + directory.resolve(path).toFile().length())
                .map(String::toLowerCase)
                .sorted()
                .reduce((a, b) -> a + b).get();

        // Hash it
        return md5(concatenatedPaths.getBytes());
    }

    private String md5(byte[] data) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data);
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
