#!/bin/bash

./mvnw clean spring-boot:build-image

# -Dspring-boot.build-image.publish=true
# -Dspring-boot.build-image.imageName=cowtowncoder/jackformer-webapp:0.5.0
