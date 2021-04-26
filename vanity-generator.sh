#!/usr/bin/env bash
export JAVA_PROGRAM_ARGS=`echo "$@"`
mvn compile exec:java -Dexec.args="$JAVA_PROGRAM_ARGS"