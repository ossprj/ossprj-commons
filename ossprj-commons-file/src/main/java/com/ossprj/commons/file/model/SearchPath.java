package com.ossprj.commons.file.model;

import java.nio.file.Path;

/**
 * SearchPath describes a base <code>Path</code> and a search depth from which a <code>List</code> of <code>Path</code>s
 * can be derived
 */
public class SearchPath {

    private Path basePath;
    private Integer searchDepth;

    public SearchPath() {
    }

    public SearchPath(Path basePath, Integer searchDepth) {
        this.basePath = basePath;
        this.searchDepth = searchDepth;
    }

    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(Path basePath) {
        this.basePath = basePath;
    }

    public Integer getSearchDepth() {
        return searchDepth;
    }

    public void setSearchDepth(Integer searchDepth) {
        this.searchDepth = searchDepth;
    }

    @Override
    public String toString() {
        return "SearchPath{" +
                "basePath=" + basePath +
                ", searchDepth=" + searchDepth +
                '}';
    }
}
