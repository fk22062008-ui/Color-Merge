#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

if [ ! -x "$JAVACMD" ]; then
  echo "ERROR: Java could not be found. Set JAVA_HOME or add java to PATH." >&2
  exit 1
fi

exec "$JAVACMD" ${JAVA_OPTS:-} ${GRADLE_OPTS:-} -Dorg.gradle.appname="$(basename "$0")" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
