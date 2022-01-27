package com.ossprj.commons.file.model;

import lombok.*;

import java.nio.file.Path;

/**
 * SearchPath describes a base <code>Path</code> and a search depth from which a <code>List</code> of <code>Path</code>s
 * can be derived
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchPath {

    @NonNull
    private Path basePath;

    @NonNull
    private Integer searchDepth;

}
