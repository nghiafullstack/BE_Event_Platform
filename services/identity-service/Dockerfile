FROM eclipse-temurin:17-jre
WORKDIR /app
# Build the jar first: mvn -pl services/identity-service -am package
COPY target/identity-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
