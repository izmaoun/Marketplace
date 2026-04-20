#C'est pour le build (Compilation)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

#C'est pour le pom.xml et le téléchargement des dependences (cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

#C'est pour copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests

#Runtime (C'est l'exécution)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

#On copie uniquement le jar généré
COPY --from=build /app/target/*.jar app.jar

#C'est le port exposé par le microservice
EXPOSE 8081

#C'est pour le lancement du microservice
ENTRYPOINT ["java", "-jar", "app.jar"]