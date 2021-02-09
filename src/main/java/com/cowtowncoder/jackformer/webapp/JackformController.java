package com.cowtowncoder.jackformer.webapp;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Main controller for Jackformation activities.
 */
@RestController
@RequestMapping("/rest")
public class JackformController
{
    // Limit input content to 10 megs for now
    private final static long MAX_INPUT_LEN = 10L * 1024 * 1024;

//    private static final Logger LOGGER = LoggerFactory.getLogger(JackformController.class);

    // Endpoint for case where content comes as Multi-part data (since it
    // is from a file user selects), output as JSON Object
    @PostMapping(value="/jackform-input-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "application/json"
    )
    public TransformResponse<String> jackformWithInputFile(
            @RequestParam(value="inputFormat", defaultValue="") String inputFormatId,
            @RequestParam(value="outputFormat", defaultValue="") String outputFormatId,
            @RequestPart(value="inputFile", required=false) MultipartFile contentFile
    ) {
        long byteLen;
        if ((contentFile == null) || (byteLen = contentFile.getSize()) <= 0L) {
            return TransformResponse.validationFail("Missing File input \"inputContent\"");
        }
        LoJack jack = LoJack.create(MAX_INPUT_LEN, inputFormatId, outputFormatId);
        TransformResponse<String> fail;

        if ((fail = jack.checkFormats()) != null) {
            return fail;
        }
        if ((fail = jack.checkInput(contentFile, byteLen)) != null) {
            return fail;
        }
        if ((fail = jack.readContents()) != null) {
            return fail;
        }
        return jack.transformAsStringResponse();
    }

    // Endpoint for "simple" case where content comes as POST payload, output as
    // JSON object
    @PostMapping(value="/jackform-input-text",
            produces = "application/json"
    )
    public TransformResponse<String> jackformWithInputText(
            @RequestParam(value="inputFormat", defaultValue="") String inputFormatId,
            @RequestParam(value="outputFormat", defaultValue="") String outputFormatId,
            // May change to `String` for better debuggability, but byte[] more efficient
            // and safer wrt character encoding
            @RequestBody byte[] inputContent
    ) {
        LoJack jack = LoJack.create(MAX_INPUT_LEN, inputFormatId, outputFormatId);
        TransformResponse<String> fail;
        if ((fail = jack.checkFormats()) != null) {
            return fail;
        }
        if ((fail = jack.checkInput(inputContent)) != null) {
            return fail;
        }
        if ((fail = jack.readContents()) != null) {
            return fail;
        }
        return jack.transformAsStringResponse();
    }

    // Endpoint for case where content comes as Multi-part data and result goes
    // as binary content, so that user gets to save output into a file.
    @PostMapping(value="/jackform-output-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> jackformWithRawOutput(
            @RequestParam(value="inputFormat", defaultValue="") String inputFormatId,
            @RequestParam(value="outputFormat", defaultValue="") String outputFormatId,
            @RequestParam(value="inputMode", defaultValue="") String inputMode,
            @RequestParam(value="inputText", defaultValue="") String contentText,
            @RequestPart(value="inputFile", required=false) MultipartFile contentFile
    ) {
        LoJack jack = LoJack.create(MAX_INPUT_LEN, inputFormatId, outputFormatId);
        TransformResponse<byte[]> fail;
        if ((fail = jack.checkFormats()) != null) {
            return _htmlForFail(fail);
        }
        String outputName;

        switch (inputMode) {
        case "input-mode-file":
            {
                long byteLen;
                if ((contentFile == null) || (byteLen = contentFile.getSize()) <= 0L) {
                    return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED).build();
                }
                fail = jack.checkInput(contentFile, byteLen);
                String name = contentFile.getName();
                int ix = name.lastIndexOf('.');
                if (ix >= 0) {
                    name = name.substring(ix);
                } else if (name.isBlank()) {
                    name = "content";
                }
                outputName = name + jack.outputFormat().suffix();
            }
            break;
        case "input-mode-text":
            if ((contentText == null) || contentText.isEmpty()) {
                return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED).build();
            }
            fail = jack.checkInput(contentText);
            outputName = "content."+outputFormatId;
            break;
        default:
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        if (fail != null) {
            return _htmlForFail(fail);
        }
        if ((fail = jack.readContents()) != null) {
            return _htmlForFail(fail);
        }
        TransformResponse<byte[]> resp = jack.transformAsByteResponse();
        if (!resp.ok) {
            return _htmlForFail(resp);
        }

        // All clear!
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename="+outputName)
                .body(resp.transformed);
    }

    private ResponseEntity<byte[]> _htmlForFail(TransformResponse<?> fail) {
        String errorMsg = fail.errorMessage;
        final String errorType = fail.errorType.toString();
        String html =
"<html><head><title>Fail: "+errorType+"</title></head>\n"
+"<body><h1>Fail: "+errorType+"</h1>\n"
+"<p>"+errorMsg+"</p>"
+"</body></html>";

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8")
//                .contentType(MediaType.TEXT_HTML)
                .body(html.getBytes(StandardCharsets.UTF_8));
    }
}
