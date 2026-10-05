package com.ossprj.commons.metadata.audio;

import java.util.*;

public class ExtractUniqueTags {

    public Map<String, Set<String>> apply(final List<Map<String, String>> sourceTags) {
        final Map<String, Set<String>> tags = new HashMap<>();

        for (final Map<String, String> sourceTag : sourceTags) {
            for (final Map.Entry<String, String> tagEntry : sourceTag.entrySet()) {

                // If the Tag already exists, then add the value
                if (tags.containsKey(tagEntry.getKey())) {
                    tags.get(tagEntry.getKey()).add(tagEntry.getValue());
                } else {
                    // ... otherwise create the Set and add this value
                    final Set<String> tagValues = new TreeSet<>();
                    tagValues.add(tagEntry.getValue());
                    tags.put(tagEntry.getKey(), tagValues);
                }
            }

        }

        return tags;
    }
}
