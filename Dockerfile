FROM openjdk:11
COPY app/build/libs/pockemon.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "pockemon.jar"]