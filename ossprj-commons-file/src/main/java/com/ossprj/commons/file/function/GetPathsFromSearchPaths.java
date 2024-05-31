package com.ossprj.commons.file.function;

import com.ossprj.commons.file.model.SearchPath;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Builds a <code>List</code> of <code>Path</code>s from a <code>List</code> of <code>SearchPath</code>s
 */
public class GetPathsFromSearchPaths {

    private final FindAllFilesAtDepth findAllFilesAtDepth = new FindAllFilesAtDepth();

    public List<Path> apply(List<SearchPath> searchPaths) {
        return apply(searchPaths, (file) -> true);
    }

    public List<Path> apply(List<SearchPath> searchPaths, final Predicate<File> filter) {

        final List<Path> paths = new LinkedList<>();

        if (searchPaths == null) {
            throw new IllegalArgumentException("searchPaths cannot be null");
        }

        for (final SearchPath searchPath : searchPaths) {
            if (searchPath == null) {
                throw new IllegalArgumentException("searchPath cannot be null: " + searchPaths);
            }
            // TODO: Validate SearchPath instance ?

            paths.addAll(findAllFilesAtDepth.apply(searchPath.getBasePath(), filter, searchPath.getSearchDepth()));
        }

        return paths;
    }
}
