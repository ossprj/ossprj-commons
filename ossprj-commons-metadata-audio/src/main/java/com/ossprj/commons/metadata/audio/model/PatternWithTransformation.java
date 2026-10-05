package com.ossprj.commons.metadata.audio.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class PatternWithTransformation {
    private Pattern pattern;
    private Optional<Function<String, String>> transformation;
}
