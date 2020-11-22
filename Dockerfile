FROM openjdk:11
COPY pokemon.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "pokemon.jar"]