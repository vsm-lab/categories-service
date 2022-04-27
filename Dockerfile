FROM maven:3.8.3-jdk-11-slim as builder
COPY ./pom.xml ./pom.xml
COPY ./src ./src
RUN mvn clean package

FROM openjdk:11-slim
COPY --from=builder target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]