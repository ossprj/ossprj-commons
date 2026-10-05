package com.ossprj.commons.metadata.audio.facade;

import com.ossprj.commons.metadata.audio.model.ExtractedTags;
import com.ossprj.commons.metadata.audio.model.Tag;

import java.util.*;

public class ExtractTagsFromName {

    private final Map<String, List<Tag>> tagsByName = new HashMap<>();

    {
        //tags.put("", Arrays.asList(new Tag("","")));

        tagsByName.put("set1", Arrays.asList(new Tag("Completion", "Set1")));
        tagsByName.put("set2", Arrays.asList(new Tag("Completion", "Set2")));
        //tagsByName.put("partialset1+set2", Arrays.asList(new Tag("Completion", "Set2")));


        tagsByName.put("sbd", Arrays.asList(new Tag("SourceType", "SBD")));
        tagsByName.put("dsbd", Arrays.asList(new Tag("SourceType", "DSBD")));
        tagsByName.put("aud", Arrays.asList(new Tag("SourceType", "AUD")));
        tagsByName.put("fob", Arrays.asList(new Tag("SourceType", "FOB")));
        tagsByName.put("mtx", Arrays.asList(new Tag("SourceType", "MATRIX")));
        tagsByName.put("fm", Arrays.asList(new Tag("SourceType", "FM")));
        tagsByName.put("matrix", Arrays.asList(new Tag("SourceType", "MATRIX")));

        //tagsByName.put("", Arrays.asList(new Tag("Source", "")));

        tagsByName.put("sbeok", Arrays.asList(new Tag("Validation", "SBEOK")));
        tagsByName.put("sbefail", Arrays.asList(new Tag("Validation", "SBEFAIL")));

        tagsByName.put("dvdf", Arrays.asList(new Tag("Format", "DVD")));
        tagsByName.put("dvd5", Arrays.asList(new Tag("Format", "DVD")));
        tagsByName.put("dvd9", Arrays.asList(new Tag("Format", "DVD")));
        tagsByName.put("mkv", Arrays.asList(new Tag("Format", "MKV")));

        tagsByName.put("flac", Arrays.asList(new Tag("Format", "FLAC")));
        tagsByName.put("flacf", Arrays.asList(new Tag("Format", "FLAC")));

        tagsByName.put("shn", Arrays.asList(new Tag("Format", "SHN")));
        tagsByName.put("shnf", Arrays.asList(new Tag("Format", "SHN")));

        tagsByName.put("flac16", Arrays.asList(new Tag("Format", "FLAC"), new Tag("AudioDepth", "16")));
        tagsByName.put("flac24", Arrays.asList(new Tag("Format", "FLAC"), new Tag("AudioDepth", "24")));

        tagsByName.put("flac1644", Arrays.asList(new Tag("Format", "FLAC"), new Tag("AudioDepth", "16"), new Tag("AudioSampleRate", "44100")));
        tagsByName.put("flac2444", Arrays.asList(new Tag("Format", "FLAC"), new Tag("AudioDepth", "24"), new Tag("AudioSampleRate", "44100")));

        tagsByName.put("flac1648", Arrays.asList(new Tag("Format", "FLAC"), new Tag("AudioDepth", "16"), new Tag("AudioSampleRate", "48000")));
        tagsByName.put("flac2448", Arrays.asList(new Tag("Format", "FLAC"), new Tag("AudioDepth", "16"), new Tag("AudioSampleRate", "48000")));

        tagsByName.put("flac2496", Arrays.asList(new Tag("Format", "FLAC"), new Tag("AudioDepth", "24"), new Tag("AudioSampleRate", "96000")));

        //tagsByName.put("flac", Arrays.asList(new Tag("AudioDepth",""),new Tag("AudioSampleRate","")));
        //tagsByName.put("flac", Arrays.asList(new Tag("AudioDepth",""),new Tag("AudioSampleRate","")));

        Arrays.asList(
                "berger",
                "bhp",
                "clugston",
                "crazyfingers",
                "cribbs",
                "dalton",
                "daweez",
                "d5scott",
                "Domyancich",
                "french",
                "Hill",
                "Hillwig",
                "Keo",
                "lamarre",
                "Mattes",
                "miller",
                "morris",
                "Scarletdog",
                "scotton",
                "severson",
                "sirmick",
                "Stubbe",
                "tobin",
                "vernon",
                "walker",
                "White"

        ).forEach(engineer -> tagsByName.put(engineer.toLowerCase(Locale.ROOT), Arrays.asList(new Tag("Engineer", engineer))));

        Arrays.asList(
                "Beyer201",
                "KM86",
                "mk4",
                "MSC", // Master SBD Cassette
                "Nak100CP4",
                "Nak300",
                "Nak700",
                "NeumannKMF4",
                "Senn421",
                "Senn441-U87",
                "UltraMatrix"
        ).forEach(source -> tagsByName.put(source.toLowerCase(Locale.ROOT), Arrays.asList(new Tag("Source", source))));

    }

    // Tags that don't give us any info and should be ignored
    private final Set<String> tagsToIgnore = new HashSet<>(Arrays.asList(
            "unknown",
            "xxxxx",
            "xxxxxx"
    ));


    public ExtractedTags perform(final String data) {
        final Set<Tag> tags = new HashSet<>();
        final StringBuilder cleanedData = new StringBuilder();

        // Make sure we have data to work with
        if (data != null && data.contains(".")) {

            // First try to split the data up by periods if has been separated that way
            final String split[] = data.split("\\.");
            for (final String element : split) {

                final String normalizedElement = element.toLowerCase(Locale.ROOT).trim();
                // Skip any empty elements (i.e. two periods next two each other)
                if (normalizedElement.isEmpty()) {
                    continue;
                }

                // See if we have an exact match with a known tag
                if (tagsByName.containsKey(normalizedElement)) {
                    tags.addAll(tagsByName.get(normalizedElement));
                } else if (normalizedElement.matches("\\d{4,6}")) {
                    // See if we have an all digit value, which should be a sourceId
                    tags.add(new Tag("SourceId", normalizedElement));
                } else if (!tagsToIgnore.contains(normalizedElement)) {
                    //cleanedData.append(element + ".");
                    if (!cleanedData.isEmpty()) {
                        cleanedData.append("|");
                    }
                    cleanedData.append(element);
                }
            }
        }
        return new ExtractedTags(data, cleanedData.toString(), tags);
    }
}
