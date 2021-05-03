#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd $DIR
export JAVA_PROGRAM_ARGS=`echo "$@"`
java -jar -server -Djava.security.egd=file:/dev/urandom dist/vanity-generator.jar $JAVA_PROGRAM_ARGS
