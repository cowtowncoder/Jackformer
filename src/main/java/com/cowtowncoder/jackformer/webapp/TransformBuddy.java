package com.cowtowncoder.jackformer.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

/**
 * Helper class that encapsulates innermost read-in/read-out transformation
 * handling.
 */
class TransformBuddy
{
    private final String PRETTY_SUFFIX = "-pretty";

    private final Map<String, DataFormat> _formats = DataFormat.mapping();

    private final String _knownFormatsDesc = DataFormat.knownFormatsAsString();

    private final long _maxInputLength;

    private final String _inputFormatId, _outputFormatId;
    private final DataFormat _inputFormat, _outputFormat;
    private final boolean _pretty;

    /**
     * Input if it comes from byte source
     */
    private byte[] _inputBytes;

    /**
     * Input if it comes as a String
     */
    private String _inputString;

    /**
     * Intermediate representation for content read.
     */
    private Object _intermediate;

    /*
    /**********************************************************************
    /* Life cycle, simple accessors
    /**********************************************************************
     */

    TransformBuddy(long maxInputLength, String inputFormatId, String outputFormatId)
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

    public static TransformBuddy create(long maxInputLength,
            String inputFormatId, String outputFormatId) {
        return new TransformBuddy(maxInputLength, inputFormatId, outputFormatId);
    }

    public DataFormat inputFormat() { return _inputFormat; }
    public DataFormat outputFormat() { return _outputFormat; }

    /*
    /**********************************************************************
    /* Public API: validation
    /**********************************************************************
     */

    public <T> TransformResponse<T> checkFormats() {
        if (_inputFormat == null) {
            return TransformResponse.validationFail("Unrecognized input format \"%s\": only following known: [%s]",
                    _inputFormatId, _knownFormatsDesc);
        }
        if (_outputFormat == null) {
            return TransformResponse.validationFail("Unrecognized output format \"%s\": only following known: [%s]",
                    _outputFormatId, _knownFormatsDesc);
        }
        return null;
    }

    public <T> TransformResponse<T> checkInput(byte[] input) {
        _inputBytes = input;
        return _checkSize(input.length);
    }

    public <T> TransformResponse<T> checkInput(String input) {
        _inputString = input;
        return _checkSize(input.length());
    }

    public <T> TransformResponse<T> checkInput(MultipartFile input, long contentLen) {
        TransformResponse<T> resp = _checkSize(contentLen);
        if (resp == null) {
            try {
                _inputBytes = input.getBytes();
            } catch (IOException e) {
                resp = TransformResponse.inputFail(
                        "Failed to read content (%s); problem: (%s) %s",
                        _bytesDesc(contentLen), e.getClass().getName(), e.getMessage());
            }
        }
        return null;
    }

    public <T> TransformResponse<T> readContents() {
        try {
            // 28-Feb-2021, tatu: CSV requires some additional magic
            if (_inputFormat == DataFormat.CSV) {
                _intermediate = _readCSVContents();
            } else {
                final ObjectMapper mapper = Jacksons.mapperFor(_inputFormat);
                if (_inputBytes != null) {
                    _intermediate = mapper.readTree(_inputBytes);
                } else {
                    _intermediate = mapper.readTree(_inputString);
                }
            }
            return null;
        } catch (Exception e) {
            return TransformResponse.inputFail(
"Failed to parse input allegedly using format \"%s\"; problem: (%s) %s",
                    _inputFormat, e.getClass().getName(), e.getMessage());
        }
    }

    public TransformResponse<String> transformAsStringResponse() {
        try {
            return TransformResponse.success(_writer().writeValueAsString(_intermediate));
        } catch (Exception e) {
            return TransformResponse.transformationFail(String.format(
"Failed to generate %s output from provided  \"%s\" content; problem: (%s) %s",
_outputFormat, _inputFormat,
e.getClass().getName(), e.getMessage()));
        }
    }

    public TransformResponse<byte[]> transformAsByteResponse() {
        try {
            return TransformResponse.success(_writer().writeValueAsBytes(_intermediate));
        } catch (Exception e) {
            return TransformResponse.transformationFail(String.format(
"Failed to generate %s output from provided  \"%s\" content; problem: (%s) %s",
_outputFormat, _inputFormat,
e.getClass().getName(), e.getMessage()));
        }
    }

    /*
    /**********************************************************************
    /* Helper methods for specialized reading/writing for some formats
    /**********************************************************************
     */

    // CSV is columnar/tabular format and we may want to configure aspects
    // bit differently...
    private Object _readCSVContents() throws Exception
    {
        CsvMapper mapper = Jacksons.csvMapper();
        // 28-Feb-2021, tatu: The first version here will simply assume/require
        //   that the first line is header and read as Array of Objects, basically.
        //   We may want to improve logics with heuristics in near future, depending
        //   on whether it seems useful (f.ex single-line input case)
        ArrayList<Object> entries = new ArrayList<>();
        ObjectReader r = mapper.readerFor(Map.class)
                .with(CsvSchema.emptySchema().withHeader());

        // Let possible initial exception be thrown here:
        final MappingIterator<Map<String,String>> it0 = (_inputBytes != null)
                ? r.readValues(_inputBytes)
                : r.readValues(_inputString)
                ;

        // But then iterate over rows:
        try (MappingIterator<Map<String,String>> it = it0) {
            try {
                while (it.hasNextValue()) {
                    entries.add(it.nextValue());
                }
            } catch (IOException e) {
                throw new IOException(String.format(
    "Failed to read data row #%d of CSV content, problem: (%s) %s",
    entries.size()+1, e.getClass().getName(), e.getMessage()));
            }
        }

        return entries;
    }

    /*
    /**********************************************************************
    /* Low-level helper methods
    /**********************************************************************
     */

    private ObjectWriter _writer() {
        ObjectWriter w = Jacksons.writerFor(_outputFormat, _pretty);
        // and XML has bit of an oddity wrt wrapping
        if (_outputFormat == DataFormat.XML) {
            w = w.withRootName("xml");
        }
        return w;
    }

    private <T> TransformResponse<T> _checkSize(long byteLen)
    {
        if (byteLen > _maxInputLength) {
            return TransformResponse.inputFail("Too big content (%s vs max %s)",
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
