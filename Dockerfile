FROM openjdk:17-jre-slim
WORKDIR /app
COPY /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]