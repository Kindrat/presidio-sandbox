#!/bin/sh

set -e

# Enables application to take PID 1 and receive SIGTERM sent by Docker stop command.
# See here https://docs.docker.com/engine/reference/builder/#/entrypoint
echo "Running command: java ${JAVA_OPTS} -jar ${WORKING_DIR}/${ARTIFACT_NAME}"
exec java ${JAVA_OPTS} -jar ${WORKING_DIR}/${ARTIFACT_NAME}
