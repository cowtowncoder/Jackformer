package com.cowtowncoder.jackformer.webapp;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return TransformResponse.success(inputContent);
    }
}
