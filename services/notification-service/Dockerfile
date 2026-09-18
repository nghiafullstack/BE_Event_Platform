FROM eclipse-temurin:17-jre
WORKDIR /app
# Build the jar first: mvn -pl services/notification-service -am package
COPY target/notification-service.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
