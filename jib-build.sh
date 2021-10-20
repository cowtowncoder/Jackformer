#!/bin/bash

# https://ashishtechmill.com/containerizing-spring-boot-application-with-jib

./mvnw clean package jib:build
