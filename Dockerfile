FROM openjdk:11
COPY app/build/libs/pokemon.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "pokemon.jar"]