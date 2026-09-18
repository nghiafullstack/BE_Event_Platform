FROM eclipse-temurin:17-jre
WORKDIR /app
# Build the jar first: mvn -pl services/api-gateway -am package
COPY target/api-gateway.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
