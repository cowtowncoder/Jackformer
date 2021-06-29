# Jackformer

Web app for flexible data transforms.

## Build, run

To build locally, use Maven, then run as [Spring Boot](https://spring.io/projects/spring-boot) app:

    ./mvnw clean spring-boot:run

and the default app will be available on localhost port 8080.

Alternatively you can build a container (Docker) image using one of 2 possible
Maven targets (see `docker-build.sh` or `jib-build.sh`.

Or you can run a pre-built image from Dockerhub with:

    docker run -p 8080:8080 cowtowncoder/jackformer-webapp:latest

## Formats supported

Support is offered for most data formats [Jackson](https://github.com/FasterXML/jackson) supports,
except that currently (0.5) the schema-based formats (Avro, CSV, Protobuf) are not supported.
Support for these formats will likely be added in near future.

Textual:

* [CSV](https://en.wikipedia.org/wiki/CSV) (input-only for 0.5.0)
* [Ion](https://en.wikipedia.org/wiki/Ion_(serialization_format)) (textual)
* [JSON](https://en.wikipedia.org/wiki/JSON)
* [(Java) Properties](https://en.wikipedia.org/wiki/.properties)
* [XML](https://en.wikipedia.org/wiki/XML)
* [YAML](https://en.wikipedia.org/wiki/YAML)

Binary:

* BSON
* [CBOR](https://en.wikipedia.org/wiki/CBOR)
* [Ion](https://en.wikipedia.org/wiki/Ion_(serialization_format)) (binary)
* [MessagePack](https://en.wikipedia.org/wiki/MessagePack)
* [Smile](https://en.wikipedia.org/wiki/Smile_(data_interchange_format))

Note: textual formats can be cut'n pasted as input, and resulted displayed on-page.
Binary formats require file upload/download. UI will enforce these limitations.

## Notes on Format support

### CSV

As of 0.5.0, CSV input requires use of single "header" line, followed by one or more
data lines. Separator has to be comma.
This would be valid input:

```
name,age
Bob,29
Bill,33
```

Data will be considered to be an Array of Objects, for purposes of transformation: names
of Object properties coming from the header line, so the example above would be similar to
YAML contents of:

```yaml
name: Bob
---
- name: "Bob"
  age: 29
- name: "Bill"
  age: 33
```

As of version 0.5.0, CSV output is not yet supported but should be added in near future.

## Ion (textual)

Pretty-printing is not yet supported for Ion (textual) output, although format
would support it: this limitation will hopefully be lifted in future.

