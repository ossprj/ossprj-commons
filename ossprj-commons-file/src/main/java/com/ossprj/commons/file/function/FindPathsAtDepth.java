package com.ossprj.commons.file.function;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FindPathsAtDepth {

    public List<Path> perform(Path basePath) {
        return perform(basePath, null, null);
    }

    public List<Path> perform(Path basePath, Integer maxDepth, Predicate<Path> pathFilter) {

        if (maxDepth != null && maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be greater than or equal to 1");
        }

        if (!basePath.toFile().exists()) {
            throw new IllegalStateException("basePath must exist");
        }

        final List<Path> paths = new LinkedList<>();

        // Find all the child path candidates
        final File[] files = basePath.toFile().listFiles();

        // If we have paths to process
        if (files != null) {
            // then find all the child paths...
            Arrays.asList(files).forEach(file -> {

                // Add current Path, no matter what
                paths.add(file.toPath());

                // If current Path is a directory...
                if (file.isDirectory()) {
                    // ...determine whether we need to recursively scan it...
                    if (maxDepth == null) {
                        paths.addAll(perform(file.toPath(), null, null));
                    } else if (maxDepth > 1) {
                        paths.addAll(perform(file.toPath(), maxDepth - 1, null));
                    }
                }
            });
        }

        // Filter the paths if a Predicate pathFilter was provided, otherwise...
        return paths.stream()
                .filter(path -> pathFilter == null || pathFilter.test(path)).collect(Collectors.toList());

    }

    public static void main(String[] args) throws InterruptedException {

        final Predicate<Path> isFile = (p) -> p.toFile().isFile();
        final Predicate<Path> isDirectory = (p) -> p.toFile().isDirectory();

        final FindPathsAtDepth findPathsAtDepth = new FindPathsAtDepth();

        System.out.println("-= Disk01 - maxDepth : 1, filter: isFile");
        findPathsAtDepth.perform(Paths.get("/home/user/archive1-ro/Disk01"), 1, isFile).forEach(System.out::println);
        Thread.sleep(5000);

        System.out.println("-= Disk01 - maxDepth : 1, filter: isDirectory");
        findPathsAtDepth.perform(Paths.get("/home/user/archive1-ro/Disk01"), 1, isDirectory).forEach(System.out::println);
        Thread.sleep(5000);

        System.out.println("-= Disk01 - maxDepth : 1, filter: null");
        findPathsAtDepth.perform(Paths.get("/home/user/archive1-ro/Disk01"), 1, null).forEach(System.out::println);
        Thread.sleep(5000);



        System.out.println("-= Disk01 - maxDepth : 2, filter: isFile");
        findPathsAtDepth.perform(Paths.get("/home/user/archive1-ro/Disk01"), 2, isFile).forEach(System.out::println);
        Thread.sleep(5000);

        System.out.println("-= Disk01 - maxDepth : 2, filter: isDirectory");
        findPathsAtDepth.perform(Paths.get("/home/user/archive1-ro/Disk01"), 2, isDirectory).forEach(System.out::println);
        Thread.sleep(5000);

        System.out.println("-= Disk01 - maxDepth : 2, filter: null");
        findPathsAtDepth.perform(Paths.get("/home/user/archive1-ro/Disk01"), 2, null).forEach(System.out::println);
        Thread.sleep(5000);


        System.out.println("-= Disk01 - maxDepth : null, filter: null");
        findPathsAtDepth.perform(Paths.get("/home/user/archive1-ro/Disk01")).forEach(System.out::println);

    }

}
