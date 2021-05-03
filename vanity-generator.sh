#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd $DIR
export MAVEN_OPTS="-server -Djava.security.egd=file:/dev/urandom"
export JAVA_PROGRAM_ARGS=`echo "$@"`
./mvnw compile exec:java -Dexec.args="$JAVA_PROGRAM_ARGS"
