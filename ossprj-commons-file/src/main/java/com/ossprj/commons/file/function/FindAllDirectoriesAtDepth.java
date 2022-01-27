package com.ossprj.commons.file.function;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Finds all directories at a given depth under a source directory
 *
 */
public class FindAllDirectoriesAtDepth implements BiFunction<Path,Integer, List<Path>> {

    @Override
    public List<Path> apply(final Path path, final Integer depth) {

        if (depth < 1) {
            throw new IllegalArgumentException("depth must be 1 or greater");
        }

        final List<Path> paths = new LinkedList<>();

        if (path.toFile().exists()) {

            // Find all the child directory candidates
            final File[] files = path.toFile().listFiles();

            // If we have files to process
            if (files != null) {
                // then find all the directories
                Arrays.asList(files).forEach(file -> {
                    if (file.isDirectory()) {
                        if (depth > 1) {
                            paths.addAll(apply(file.toPath(), depth - 1));
                        } else {
                            paths.add(file.toPath());
                        }
                    }
                });
            }

        }

        return paths;
    }

}
