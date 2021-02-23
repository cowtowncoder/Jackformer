# Jackformer

Web app for flexible data transforms.

## Build, run

To build locally, use Maven, then run as [Spring Boot](https://spring.io/projects/spring-boot) app:

    ./mvnw clean spring-boot:run

and the default app will be available on localhost port 9090.

Alternatively there is rudimentary `Dockerfile` to use after Maven build (`./mvnw clean package`)
for something like:

    docker build -t mydockerhubrepo/jackformer .
    docker run -p 9090:9090 mydockerhubrepo/jackformer

## Formats supported

Support is offered for most data formats [Jackson](https://github.com/FasterXML/jackson) supports,
except that currently (0.5) the schema-based formats (Avro, CSV, Protobuf) are not supported.
Support for these formats will likely be added in near future.

Textual:

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







