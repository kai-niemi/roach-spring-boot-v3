#!/bin/bash

basedir=.
jarfile=${basedir}/target/sandbox.jar

if [ ! -f "$jarfile" ]; then
    ./mvnw clean install
fi

java -jar $jarfile $*