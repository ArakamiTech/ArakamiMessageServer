FROM openjdk:17-jdk-slim
VOLUME /tmp
EXPOSE 8080
COPY ./target/ArakamiMessageServer-1.0-SNAPSHOT.jar ./
ENTRYPOINT ["java","-jar","./ArakamiMessageServer-1.0-SNAPSHOT.jar"]