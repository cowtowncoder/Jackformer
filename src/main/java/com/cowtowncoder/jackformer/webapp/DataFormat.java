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

    JSON("json", "JSON", false),
    PROPERTIES("properties", "Properties", false),
    XML("xml", "XML", false),
    YAML("yaml", "YAML", false),

    // then binary
    CBOR("cbor", "CBOR", true),
    SMILE("smile", "Smile", true),

    ;

    private final String id;
    private final String desc;
    private final boolean isBinary;

    private DataFormat(String n, String d, boolean binary) {
        id = n;
        desc = d;
        isBinary = binary;
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

    public String suffix() {
        // For now, same as... id?
        return "."+id;
    }

    public boolean isBinaryFormat() {
        return isBinary;
    }
    
    @Override
    public String toString() {
        return desc;
    }
}
