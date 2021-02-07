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

    JSON("json", "JSON"),
    PROPERTIES("properties", "Properties"),
    XML("xml", "XML"),
    YAML("yaml", "YAML")

    // then binary

    ;

    public final String id;
    public final String desc;

    private DataFormat(String n, String d) {
        id = n;
        desc = d;
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

    @Override
    public String toString() {
        return desc;
    }
}
