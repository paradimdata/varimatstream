FROM maven:3.8.1-openjdk-11 AS build
RUN mkdir -p /app/src
COPY src /app/src

COPY pom.xml /app

RUN mvn -f /app/pom.xml clean package

FROM openjdk:11-jre-slim
FROM flink:1.17.0
COPY --from=build /app/target/varimat-stream-processing-1.3.jar /opt/flink/lib/varimat-stream-processing-1.3.jar
COPY --from=build /app/target/varimat-stream-processing-1.3-jar-with-dependencies.jar /opt/flink/lib/varimat-stream-processing-1.3-jar-with-dependencies.jar

RUN mkdir -p /empad/mask
COPY mask/mask.mat /empad/mask

RUN mkdir -p /empad/means
COPY means /empad/means

RUN mkdir -p /empad/temp
COPY temp /empad/temp

RUN mkdir -p /empad/temp/prc
COPY output /empad//temp/prc

RUN mkdir -p /empad/output
COPY output /empad/output

COPY --from=build /app/target/dependency-jars /opt/flink/lib/dependency-jars

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xms14g", "-Xmx26g" ,"/opt/flink/lib/varimat-stream-processing-1.3.jar"]