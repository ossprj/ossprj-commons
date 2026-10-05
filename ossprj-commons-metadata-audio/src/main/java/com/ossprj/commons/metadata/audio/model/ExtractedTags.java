package com.ossprj.commons.metadata.audio.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ExtractedTags {

    private final String data;
    private final String cleanedData;
    private final Set<Tag> tags;

}
