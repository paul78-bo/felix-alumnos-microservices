FROM eclipse-temurin:17
WORKDIR /app
COPY target/*.jar app.jar
# AÑADE ESTA LÍNEA:
COPY src/main/resources/application.yml ./application.yml
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]