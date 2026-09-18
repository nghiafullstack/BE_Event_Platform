FROM eclipse-temurin:17-jre
WORKDIR /app
# Build the jar first: mvn -pl services/event-service -am package
COPY target/event-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
