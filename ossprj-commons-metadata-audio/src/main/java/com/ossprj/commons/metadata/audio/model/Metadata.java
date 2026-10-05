package com.ossprj.commons.metadata.audio.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class Metadata {

    private final String artist;
    private final String date;
    private final String data;
    private final String cleanedData;
    private final Set tags;

}
