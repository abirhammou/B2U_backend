FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENV SPRING_DEVTOOLS_RESTART_ENABLED=false
ENV SPRING_DEVTOOLS_LIVERELOAD_ENABLED=false
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.data.mongodb.uri=mongodb://mongodb:27017/B2U_hub"]
