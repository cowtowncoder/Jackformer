package com.cowtowncoder.jackformer.webapp;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Main controller for Jackformation activities.
 */
@RestController
@RequestMapping("/rest")
public class JackformController
{
    private final String PRETTY_SUFFIX = "-pretty";

    // Limit input content to 10 megs for now
    private final static long MAX_INPUT_LEN = 10L * 1024 * 1024;

    private final Map<String, DataFormat> _formats = DataFormat.mapping();

    private final String _knownFormatsDesc = DataFormat.knownFormatsAsString();

    // Endpoint for form-data case needed with File upload
    @PostMapping(value="/jackform-with-form",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "application/json"
    )
    public TransformResponse jackItWithPost(
            @RequestParam(value="inputFormat", defaultValue="") String inputFormatId,
            @RequestParam(value="outputFormat", defaultValue="") String outputFormatId,
            @RequestPart(value="inputContent", required=false) MultipartFile contentPart
    ) {
        long byteLen;
        if ((contentPart == null) || (byteLen = contentPart.getSize()) <= 0L) {
            return TransformResponse.validationFail("Missing File input \"inputContent\"");
        }
        TransformResponse fail = _checkSize(byteLen);
        if (fail != null) {
            return fail;
        }
        byte[] inputContent;
        try {
            inputContent = contentPart.getBytes();
        } catch (IOException e) {
            return TransformResponse.inputFail(
"Failed to read content (%s); problem: (%s) %s",
                _bytesDesc(byteLen), e.getClass().getName(), e.getMessage());
        }
        return _jackform(inputFormatId, outputFormatId, inputContent);
    }

    // Endpoint for "simple" case without File upload
    @PostMapping(value="/jackform",
            produces = "application/json"
    )
    public TransformResponse jackItWithParams(
            @RequestParam(value="inputFormat", defaultValue="") String inputFormatId,
            @RequestParam(value="outputFormat", defaultValue="") String outputFormatId,
            // May change to `String` for better debuggability, but byte[] more efficient
            // and safer wrt character encoding
            @RequestBody byte[] inputContent
    ) {
        TransformResponse fail = _checkSize(inputContent.length);
        if (fail != null) {
            return fail;
        }
        return _jackform(inputFormatId, outputFormatId, inputContent);
    }

    private TransformResponse _jackform(String inputFormatId, String outputFormatId,
            byte[] inputContent)
    {
        final DataFormat inputFormat = _formats.get(inputFormatId);
        if (inputFormat == null) {
            return TransformResponse.validationFail("Unrecognized input format \"%s\": only following known: [%s]",
                    inputFormatId, _knownFormatsDesc);
        }
        final boolean prettyOut = outputFormatId.endsWith(PRETTY_SUFFIX);
        if (prettyOut) {
            outputFormatId = outputFormatId.substring(0, outputFormatId.length() - PRETTY_SUFFIX.length());
        }
        final DataFormat outputFormat = _formats.get(outputFormatId);
        if (outputFormat == null) {
            return TransformResponse.validationFail("Unrecognized output format \"%s\": only following known: [%s]",
                    outputFormatId, _knownFormatsDesc);
        }
        Object intermediate;
        ObjectMapper mapper = Jacksons.mapperFor(inputFormat);

        try {
            intermediate = mapper.readTree(inputContent);
//            intermediate = mapper.readValue(inputContent, Object.class);
        } catch (Exception e) {
            return TransformResponse.inputFail(
"Failed to parse input allegedly using format \"%s\"; problem: (%s) %s",
                    inputFormat, e.getClass().getName(), e.getMessage());
        }

        ObjectWriter w = Jacksons.writerFor(outputFormat, prettyOut);
        // and XML has bit of an oddity wrt wrapping
        if (outputFormat == DataFormat.XML) {
            w = w.withRootName("xml");
        }

        String transformed;
        try {
            transformed = w.writeValueAsString(intermediate);
        } catch (Exception e) {
            return TransformResponse.inputFail(
"Failed to generate %s output from provided  \"%s\" content; problem: (%s) %s",
outputFormat, inputFormat,
e.getClass().getName(), e.getMessage());
        }

        return TransformResponse.success(transformed);
    }

    private TransformResponse _checkSize(long byteLen)
    {
        if (byteLen > MAX_INPUT_LEN) {
            return TransformResponse.validationFail("Too big content (%s vs max %s)",
                    _bytesDesc(byteLen),
                    _bytesDesc(MAX_INPUT_LEN));
        }
        return null;
    }

    private String _bytesDesc(long len) {
        if (len < 2000L) {
            return len+" bytes";
        }
        double kb = len / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format("%.1f MB", mb);
    }
}
