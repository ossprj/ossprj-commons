package com.ossprj.commons.file.function;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Finds all files at a given depth under a source directory
 */
public class FindAllFilesAtDepth {

    public List<Path> apply(final Path path, final Integer depth) {
        return apply(path, (file) -> true, depth);
    }

    public List<Path> apply(final Path path, final Predicate<File> filter, final Integer depth) {
        //System.out.println("apply: " + path + " - " + depth + " - " + filter.toString());

        if (depth < 1) {
            throw new IllegalArgumentException("depth must be 1 or greater");
        }

        final List<Path> paths = new LinkedList<>();

        if (path.toFile().exists()) {

            // Find all the child directory candidates
            final File[] files = path.toFile().listFiles();

            // If we have files to process
            if (files != null) {

                // If depth is 1 then we want to stop traversing directories and start matching files
                if (depth == 1) {
                    Arrays.stream(files)
                            .filter(filter)
                             .forEach(file -> paths.add(file.toPath()));
                } else {
                    // otherwise traverse the directories for deeper matches
                    Arrays.stream(files)
                            .filter(File::isDirectory)
                            .forEach(file -> paths.addAll(apply(file.toPath(), filter, depth - 1)));
                }
            }
        }

        return paths;
    }


    public static void main(String[] args) {


        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid2/Movie.Archive"), File::isDirectory, 1));
        System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid2/Movie.Archive"), File::isFile, 1));

        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), File::isFile, 1));
        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), File::isDirectory, 1));


        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), (file) -> true, 1));

        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), (file) -> !file.isDirectory(), 2));
        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), File::isFile, 2));

        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), (file) -> file.isDirectory(), 1));
        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), File::isDirectory, 1));


        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), File::isFile, 2));

        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music/Phish"), File::isFile, 2));

        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), File::isDirectory, 2));


        //System.out.println(new FindAllFilesAtDepth().apply(Paths.get("/raid1/Music"), 1));
    }

}
