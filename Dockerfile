FROM openjdk:11
COPY pokemon.jar .
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "pokemon.jar"]