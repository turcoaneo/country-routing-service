FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy your already-built JAR
COPY target/country-routing-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]