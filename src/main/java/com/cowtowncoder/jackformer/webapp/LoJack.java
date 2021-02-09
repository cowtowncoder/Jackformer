package com.cowtowncoder.jackformer.webapp;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Helper class that encapsulates innermost read-in/read-out transformation
 * handling.
 */
class LoJack
{
    private final String PRETTY_SUFFIX = "-pretty";

    private final Map<String, DataFormat> _formats = DataFormat.mapping();

    private final String _knownFormatsDesc = DataFormat.knownFormatsAsString();

    private final long _maxInputLength;

    private final String _inputFormatId, _outputFormatId;
    private final DataFormat _inputFormat, _outputFormat;
    private final boolean _pretty;

    private byte[] _inputBytes;
    private String _inputString;

    private Object _intemediate;
    
    LoJack(long maxInputLength, String inputFormatId, String outputFormatId)
    {
        _maxInputLength = maxInputLength;
        _inputFormatId = inputFormatId;
        _inputFormat = _formats.get(inputFormatId);
        _pretty = outputFormatId.endsWith(PRETTY_SUFFIX);
        if (_pretty) {
            outputFormatId = outputFormatId.substring(0, outputFormatId.length() - PRETTY_SUFFIX.length());
        }
        _outputFormatId = outputFormatId;
        _outputFormat = _formats.get(outputFormatId);
    }

    public static LoJack create(long maxInputLength,
            String inputFormatId, String outputFormatId) {
        return new LoJack(maxInputLength, inputFormatId, outputFormatId);
    }

    public DataFormat inputFormat() { return _inputFormat; }
    public DataFormat outputFormat() { return _outputFormat; }

    public String checkFormats() {
        if (_inputFormat == null) {
            return String.format("Unrecognized input format \"%s\": only following known: [%s]",
                    _inputFormatId, _knownFormatsDesc);
        }
        if (_outputFormat == null) {
            return String.format("Unrecognized output format \"%s\": only following known: [%s]",
                    _outputFormatId, _knownFormatsDesc);
        }
        return null;
    }

    public String checkInput(byte[] input) {
        _inputBytes = input;
        return _checkSize(input.length);
    }

    public String checkInput(String input) {
        _inputString = input;
        return _checkSize(input.length());
    }

    public String checkInput(MultipartFile input, long contentLen) {
        String msg = _checkSize(contentLen);
        if (msg != null) {
            return msg;
        }
        try {
            _inputBytes = input.getBytes();
        } catch (IOException e) {
            return String.format("Failed to read content (%s); problem: (%s) %s",
                    _bytesDesc(contentLen), e.getClass().getName(), e.getMessage());
        }
        return null;
    }

    public String readContents() {
        try {
            final ObjectMapper mapper = Jacksons.mapperFor(_inputFormat);
            if (_inputBytes != null) {
                _intemediate = mapper.readTree(_inputBytes);
            } else {
                _intemediate = mapper.readTree(_inputString);
            }
            return null;
        } catch (Exception e) {
            return String.format(
"Failed to parse input allegedly using format \"%s\"; problem: (%s) %s",
                    _inputFormat, e.getClass().getName(), e.getMessage());
        }
        
    }

    public TransformResponse transformAsResponse() {
        try {
            return TransformResponse.success(_writer().writeValueAsString(_intemediate));
        } catch (Exception e) {
            return TransformResponse.inputFail(String.format(
"Failed to generate %s output from provided  \"%s\" content; problem: (%s) %s",
_outputFormat, _inputFormat,
e.getClass().getName(), e.getMessage()));
        }
    }

    public ResponseEntity<byte[]> transformAsEntity(String filename) {
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename="+filename)
                    .body(_writer().writeValueAsBytes(_intemediate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ObjectWriter _writer() {
        ObjectWriter w = Jacksons.writerFor(_outputFormat, _pretty);
        // and XML has bit of an oddity wrt wrapping
        if (_outputFormat == DataFormat.XML) {
            w = w.withRootName("xml");
        }
        return w;
    }

    private String _checkSize(long byteLen)
    {
        if (byteLen > _maxInputLength) {
            return String.format("Too big content (%s vs max %s)",
                    _bytesDesc(byteLen),
                    _bytesDesc(_maxInputLength));
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
