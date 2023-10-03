#!/bin/bash

# Runs typechecking in a continuous integration job.

# Required argument $1 is one of:
#   formatter, index, interning, lock, nullness, regex, signature, nothing


# Fail the whole script if any command fails
set -e

## Build Checker Framework
if [ -z "${CHECKERFRAMEWORK}" ] && [ ! -d "../checker-framework/" ] ; then
  (cd .. && git clone --depth 1 https://github.com/typetools/checker-framework.git)
  CHECKERFRAMEWORK=$(cd ../checker-framework/ >/dev/null 2>&1 && pwd -P)
  export CHECKERFRAMEWORK
fi

# This also builds annotation-tools.
# Run assembleForJavac because it does not build the javadoc, so it is faster than assemble.
(cd "${CHECKERFRAMEWORK}" && ./gradlew assembleForJavac --console=plain -Dorg.gradle.internal.http.socketTimeout=60000 -Dorg.gradle.internal.http.connectionTimeout=60000 )

# As of 7/27/2019, there are only annotations for:
#  * index
#  * nullness
#  * signedness
# but run the other jobs anyway.

if [[ "$1" == "formatter" ]]; then
  (cd guava && mvn -B compile -P checkerframework-local -Dcheckerframework.checkers=org.checkerframework.checker.formatter.FormatterChecker)
elif [[ "$1" == "index" ]]; then
  (cd guava && mvn -B compile -P checkerframework-local -Dcheckerframework.checkers=org.checkerframework.checker.index.IndexChecker)
elif [[ "$1" == "interning" ]]; then
  (cd guava && mvn -B compile -P checkerframework-local -Dcheckerframework.checkers=org.checkerframework.checker.interning.InterningChecker)
elif [[ "$1" == "lock" ]]; then
  (cd guava && mvn -B compile -P checkerframework-local -Dcheckerframework.checkers=org.checkerframework.checker.lock.LockChecker)
elif [[ "$1" == "nullness" ]]; then
  (cd guava && mvn -B compile -P checkerframework-local -Dcheckerframework.checkers=org.checkerframework.checker.nullness.NullnessChecker)
elif [[ "$1" == "regex" ]]; then
  (cd guava && mvn -B compile -P checkerframework-local -Dcheckerframework.checkers=org.checkerframework.checker.regex.RegexChecker)
elif [[ "$1" == "signature" ]]; then
  (cd guava && mvn -B compile -P checkerframework-local -Dcheckerframework.checkers=org.checkerframework.checker.signature.SignatureChecker)
elif [[ "$1" == "signedness" ]]; then
  (cd guava && mvn -B compile -P checkerframework-local -Dcheckerframework.checkers=org.checkerframework.checker.signedness.SignednessChecker)
elif [[ "$1" == "nothing" ]]; then
  true
else
  echo "Bad argument '$1' to build.sh"
  false
fi
