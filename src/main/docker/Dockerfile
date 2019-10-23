FROM openjdk:8-jre-slim

ENV WORKING_DIR="/usr/local/@project.name@" \
    ARTIFACT_NAME="@project.name@.jar" \
    JAVA_OPTS="-Xmx256m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:+ExitOnOutOfMemoryError"

COPY @project.name@.jar $WORKING_DIR
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
