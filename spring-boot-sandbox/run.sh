#!/bin/bash

basedir=.
jarfile=${basedir}/target/oom.jar

if [ ! -f "$jarfile" ]; then
    ./mvnw clean install
fi

java -jar $jarfile $*