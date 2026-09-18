FROM eclipse-temurin:17-jre
WORKDIR /app
# Build the jar first: mvn -pl services/customer-service -am package
COPY target/customer-service.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
