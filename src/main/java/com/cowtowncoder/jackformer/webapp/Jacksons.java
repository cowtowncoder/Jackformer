package com.cowtowncoder.jackformer.webapp;

import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.ion.IonFactory;
import com.fasterxml.jackson.dataformat.ion.IonObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.undercouch.bson4jackson.BsonFactory;

/**
 * Helper class used to access various format backends for Jackson transformations.
 * Uses lazy loading of instances via wrapper classes to reduce need to 
 * load everything upfront (may or may not matter performance-wise)
 */
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
        case BSON:
            return BSONWrapper.mapper();
        case CSV: // usually not accessed through this method but...
            return csvMapper();
        case CBOR:
            return CBORWrapper.mapper();
        case ION_BINARY:
            return IonWrapper.mapperForBinary();
        case ION_TEXTUAL:
            return IonWrapper.mapperForTextual();
        case JSON:
            return JSON_MAPPER;
        case MSGPACK:
            return MsgPackWrapper.mapper();
        case PROPERTIES:
            return PropertiesWrapper.mapper();
        case SMILE:
            return SmileWrapper.mapper();
        case TOML:
            return TomlWrapper.mapper();
        case XML:
            return XmlWrapper.mapper();
        case YAML:
            return YamlWrapper.mapper();
        default:
            throw new IllegalArgumentException("Internal error: no ObjectMapper known for format "+f);
        }
    }

    public static CsvMapper csvMapper()
    {
        return CSVWrapper.mapper();
    }

    static class BSONWrapper {
        private final static ObjectMapper wrapped = new ObjectMapper(new BsonFactory());

        public static ObjectMapper mapper() { return wrapped; }
    }

    static class CBORWrapper {
        private final static ObjectMapper wrapped = CBORMapper.builder().build();

        public static ObjectMapper mapper() { return wrapped; }
    }

    static class CSVWrapper {
        // Note: need to keep full type to generate CsvSchema as necessary
        private final static CsvMapper wrapped = CsvMapper.builder().build();

        public static CsvMapper mapper() { return wrapped; }
    }

    static class IonWrapper {
        private final static IonObjectMapper wrappedBinary = IonObjectMapper.builder(
                IonFactory.forBinaryWriters())
                .build();
        private final static IonObjectMapper wrappedTextual = IonObjectMapper.builder(
                IonFactory.forTextualWriters())
                .build();

        public static ObjectMapper mapperForBinary() { return wrappedBinary; }
        public static ObjectMapper mapperForTextual() { return wrappedTextual; }
    }

    static class MsgPackWrapper {
        private final static ObjectMapper wrapped = new ObjectMapper(new MessagePackFactory());

        public static ObjectMapper mapper() { return wrapped; }
    }

    static class PropertiesWrapper {
        private final static ObjectMapper wrapped = JavaPropsMapper.builder().build();

        public static ObjectMapper mapper() { return wrapped; }
    }

    static class SmileWrapper {
        // Optimize for size: enable shared string value back-refs; disable 7-bit
        // binary encoding (embed as raw binary)
        private final static ObjectMapper wrapped = SmileMapper.builder(
                SmileFactory.builder()
                    .enable(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES)
                    .disable(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT)
                    .build()
        ).build();

        public static ObjectMapper mapper() { return wrapped; }
    }

    static class TomlWrapper {
        private final static ObjectMapper wrapped = TomlMapper.builder().build();

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
