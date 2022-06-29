FROM openjdk:8-alpine
ADD target/collector-1.0-SNAPSHOT.jar /collector-1.0-SNAPSHOT.jar
ADD config.properties /config.properties
CMD ["java", "-jar", "collector-1.0-SNAPSHOT.jar"]
