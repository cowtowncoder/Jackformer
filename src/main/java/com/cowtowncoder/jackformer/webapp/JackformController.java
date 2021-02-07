package com.cowtowncoder.jackformer.webapp;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * Place where trans... I mean, JACKformations happen.
 */
@RestController
@RequestMapping("/rest")
public class JackformController
{
    private final String PRETTY_SUFFIX = "-pretty";

    private final Map<String, DataFormat> _formats = DataFormat.mapping();

    private final String _knownFormatsDesc = DataFormat.knownFormatsAsString();

    @GetMapping(value="/jackform",
            produces = "application/json"
    )
    public TransformResponse jackIt(
            @RequestParam(value="inputFormat", defaultValue="") String inputFormatId,
            @RequestParam(value="outputFormat", defaultValue="") String outputFormatId,
            @RequestParam(value="inputContent", defaultValue="") String inputContent
    ) {
        final boolean prettyOut = inputFormatId.endsWith(PRETTY_SUFFIX);
        if (prettyOut) {
            inputFormatId = inputFormatId.substring(0, inputFormatId.length() - PRETTY_SUFFIX.length());
        }
        final DataFormat inputFormat = _formats.get(inputFormatId);
        if (inputFormat == null) {
            return TransformResponse.validationFail("Unrecognized input format \"%s\": only following known: [%s]",
                    inputFormatId, _knownFormatsDesc);
        }
        final DataFormat outputFormat = _formats.get(outputFormatId);
        if (outputFormat == null) {
            return TransformResponse.validationFail("Unrecognized output format \"%s\": only following known: [%s]",
                    inputFormatId, _knownFormatsDesc);
        }
        JsonNode intermediate;
        ObjectMapper mapper = mapperFor(inputFormat);

        try {
            intermediate = mapper.readTree(inputContent);
        } catch (Exception e) {
            return TransformResponse.inputFail(
"Failed to parse input allegedly using format \"%s\"; problem: (%s) %s",
                    inputFormat, e.getClass().getName(), e.getMessage());
        }

        mapper = mapperFor(outputFormat);
        String transformed;
        try {
            ObjectWriter w = mapper.writer();
            if (prettyOut) {
                w = w.withDefaultPrettyPrinter();
            }
            transformed = w.writeValueAsString(intermediate);
        } catch (Exception e) {
            return TransformResponse.inputFail(
"Failed to generate %s output from provided  \"%s\" content; problem: (%s) %s",
outputFormat, inputFormat,
e.getClass().getName(), e.getMessage());
        }

        return TransformResponse.success(transformed);
    }

    private ObjectMapper mapperFor(DataFormat f) {
        switch (f) {
        case JSON:
            return JSON_MAPPER;
        case PROPERTIES:
            return PROPERTIES_MAPPER;
        case XML:
            return XML_MAPPER;
        case YAML:
            return YAML_MAPPER;
        default:
            throw new IllegalArgumentException("Internal error: no ObjectMapper known for format "+f);
        }
    }

    private final static ObjectMapper JSON_MAPPER = JsonMapper.builder().build();

    private final static ObjectMapper PROPERTIES_MAPPER = JavaPropsMapper.builder().build();
    private final static ObjectMapper XML_MAPPER = XmlMapper.builder().build();
    private final static ObjectMapper YAML_MAPPER = YAMLMapper.builder().build();
}
