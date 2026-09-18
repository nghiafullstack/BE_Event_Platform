FROM eclipse-temurin:17-jre
WORKDIR /app
# Build the jar first: mvn -pl services/catalog-service -am package
COPY target/catalog-service.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
