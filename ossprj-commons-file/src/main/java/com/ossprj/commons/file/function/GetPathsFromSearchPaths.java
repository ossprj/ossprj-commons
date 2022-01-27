package com.ossprj.commons.file.function;

import com.ossprj.commons.file.model.SearchPath;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Builds a <code>List</code> of <code>Path</code>s from a <code>List</code> of <code>SearchPath</code>s
 *
 */
public class GetPathsFromSearchPaths implements Function<List<SearchPath>, List<Path>> {

    private final FindAllDirectoriesAtDepth findAllDirectoriesAtDepth = new FindAllDirectoriesAtDepth();

    @Override
    public List<Path> apply(List<SearchPath> searchPaths) {

        final List<Path> paths = new LinkedList<>();

        if (searchPaths == null) {
            throw new IllegalArgumentException("searchPaths cannot be null");
        }

        for (final SearchPath searchPath : searchPaths) {
            if (searchPath == null) {
                throw new IllegalArgumentException("searchPath cannot be null: " + searchPaths);
            }
            // TODO: Validate SearchPath instance ?

            paths.addAll(findAllDirectoriesAtDepth.apply(searchPath.getBasePath(), searchPath.getSearchDepth()));
        }

        return paths;
    }
}
