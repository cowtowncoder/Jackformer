package com.cowtowncoder.jackformer.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public final class Jacksons
{
    // We'll construct vanilla mapper eagerly, as that is always needed anyway,
    // but scope others into helper classes
    private final static ObjectMapper JSON_MAPPER = JsonMapper.builder().build();

    public static ObjectWriter writerFor(DataFormat f, boolean pretty) {
        ObjectWriter w = mapperFor(f).writer();
        if (pretty) {
            w = w.withDefaultPrettyPrinter();
        }
        return w;
    }

    public static ObjectMapper mapperFor(DataFormat f)
    {
        switch (f) {
        case JSON:
            return JSON_MAPPER;
        case PROPERTIES:
            return PropertiesWrapper.mapper();
        case XML:
            return XmlWrapper.mapper();
        case YAML:
            return YamlWrapper.mapper();
        default:
            throw new IllegalArgumentException("Internal error: no ObjectMapper known for format "+f);
        }
    }

    static class PropertiesWrapper {
        private final static ObjectMapper wrapped = JavaPropsMapper.builder().build();

        public static ObjectMapper mapper() { return wrapped; }
    }

    static class XmlWrapper {
        private final static ObjectMapper wrapped = XmlMapper.builder().build();

        public static ObjectMapper mapper() { return wrapped; }
    }

    static class YamlWrapper {
        private final static ObjectMapper wrapped = YAMLMapper.builder().build();

        public static ObjectMapper mapper() { return wrapped; }
    }
}
