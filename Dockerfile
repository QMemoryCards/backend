FROM gradle:jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Runtime image
FROM openjdk:26-ea-21-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
