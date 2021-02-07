package com.cowtowncoder.jackformer.webapp;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple enumeration of supported input and output formats.
 */
public enum DataFormat
{
    // First textual

    JSON("json"),
    PROPERTIES("properties"),
    XML("xml"),
    YAML("yaml")

    // then binary

    ;

    public final String id;

    private DataFormat(String n) {
        id = n;
    }

    public static Map<String, DataFormat> mapping() {
        Map<String, DataFormat> map = new LinkedHashMap<>();
        for (DataFormat f : values()) {
            map.put(f.id(), f);
        }
        return map;
    }

    public static String knownFormatsAsString() {
        return Arrays.asList(DataFormat.values())
                .stream()
                .map(e -> e.id())
                .collect(Collectors.joining(", "));
    }

    public String id() {
        return id;
    }
}
