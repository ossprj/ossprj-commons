package com.ossprj.commons.metadata.audio.flac.function;

import com.ossprj.commons.metadata.audio.model.TagName;
import org.jflac.FLACDecoder;
import org.jflac.metadata.Metadata;
import org.jflac.metadata.StreamInfo;
import org.jflac.metadata.VorbisComment;
import org.jflac.metadata.VorbisString;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtractMetadataFromFlacFile implements Function<Path, Map<String, String>> {

    private final Map<String, String> knownTags = new HashMap<>(Arrays.stream(TagName.values())
            .collect(Collectors.toMap(entry -> entry.toString().toLowerCase(Locale.ROOT), Object::toString)));

    @Override
    public Map<String, String> apply(final Path pathToFlacFile) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("AudioFormat", "FLAC");

        try (final FileInputStream fileInputStream = new FileInputStream(pathToFlacFile.toFile())) {

            final Metadata[] metadataArray = new FLACDecoder(fileInputStream).readMetadata();

            Arrays.stream(metadataArray).forEach(metadata -> {

                // StreamInfo
                if (metadata.getClass().isAssignableFrom(StreamInfo.class)) {
                    final StreamInfo streamInfo = (StreamInfo) metadata;

                    tags.put("AudioBitsPerSample", String.valueOf(streamInfo.getBitsPerSample()));
                    tags.put("AudioChannels", String.valueOf(streamInfo.getChannels()));
                    tags.put("AudioSampleRate", String.valueOf(streamInfo.getSampleRate()));
                }

                // VorbisComment
                if (metadata.getClass().isAssignableFrom(VorbisComment.class)) {
                    final VorbisComment vorbisComment = (VorbisComment) metadata;
                    for (int x = 0; x < vorbisComment.getNumComments(); x++) {
                        final VorbisString vorbisString = vorbisComment.getComment(x);
                        //System.out.println("vorbisString: " + vorbisString);
                        final String[] commentParts = vorbisString.toString().split("=");
                        // Lowercase the tag name so we can match easier
                        final String commentName = commentParts[0]
                                .toLowerCase(Locale.ROOT)
                                .replaceAll(" ","")
                                .replaceAll("_","")
                                .replaceAll("<","")
                                .replaceAll(">","")
                                ;
                        final String commentValue = (commentParts.length == 2 && commentParts[1] != null) ? commentParts[1].trim() : "";

                        if (knownTags.containsKey(commentName)) {
                            tags.put(knownTags.get(commentName), commentValue);
                        } else {
                            System.out.println("Unknown Tag: " + commentName);
                            tags.put(commentName, commentValue);
                        }

                        /*if ("ALBUM".equals(commentName)) {
                            tags.put(TagName.Album, commentValue);
                        } else if ("ARTIST".equals(commentName)) {
                            tags.put(TagName.Artist, commentValue);
                        } else if ("COMMENT".equals(commentName)) {
                            tags.put(TagName.Comment, commentValue);
                        } else if ("COPYRIGHT".equals(commentName)) {
                            tags.put(TagName.Copyright, commentValue);
                        } else if ("DATE".equals(commentName)) {
                            tags.put(TagName.Date, commentValue);
                        } else if ("DISCNUMBER".equals(commentName)) {
                            tags.put(TagName.DiscNumber, commentValue);
                        } else if ("ENSEMBLE".equals(commentName)) {
                            tags.put(TagName.Ensemble, commentValue);
                        } else if ("GENRE".equals(commentName)) {
                            tags.put(TagName.Genre, commentValue);
                        } else if ("TITLE".equals(commentName)) {
                            tags.put(TagName.Title, commentValue);
                        } else if ("TRACKNUMBER".equals(commentName)) {
                            tags.put(TagName.TrackNumber, commentValue);
                        }*/
                    }
                }
            });

            return tags;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        final ExtractMetadataFromFlacFile extractMetadataFromFlacFile = new ExtractMetadataFromFlacFile();

        final Map<String, String> tags = extractMetadataFromFlacFile
                .apply(Paths.get("/media/user/Old/Archive1.Sorted/Ratdog/RatDog - 2009-08-22 Eugene, OR/bwr090822d1_01_Jack_Straw.flac"));

        System.out.println(tags);

    }


}
