FROM maven:3.8.1-openjdk-11 AS build

COPY src /app/src
COPY pom.xml /app

RUN mvn -f /app/pom.xml clean package


#
# Package stage
#
#FROM openjdk:11-jre-slim
FROM flink:1.17.0
COPY --from=build /app/target/*.jar /opt/flink/lib
EXPOSE 8080
#ENTRYPOINT ["java","-jar","/usr/local/lib/varimat-stream-processing-1.2.jar"]