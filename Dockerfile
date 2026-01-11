FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms128m -Xmx384m \
-XX:MaxMetaspaceSize=128m \
-XX:+UseSerialGC \
-XX:MaxRAMPercentage=75 \
-XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
