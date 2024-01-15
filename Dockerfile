# Usar una imagen base de Maven para compilar la aplicación
FROM maven:3.8.1-openjdk-17-slim AS build

# Establecer el directorio de trabajo en /app
WORKDIR /app

# Copiar el archivo pom.xml y los archivos fuente
COPY pom.xml .
COPY src src

# Empaquetar la aplicación como un archivo JAR
RUN mvn clean package spring-boot:repackage -DskipTests

# Crear una imagen Docker con la aplicación compilada
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update

# Establecer el directorio de trabajo en /app
WORKDIR /app
# Copiar el archivo JAR de la fase de compilación anterior
COPY --from=build /app/target/appointment-back-0.0.1-SNAPSHOT.jar /app/app.jar

# Exponer el puerto en el que se ejecuta la aplicación Spring Boot
EXPOSE 8080
ENV TZ=America/Santiago

# Comando para ejecutar la aplicación Spring Boot cuando se inicie el contenedor
CMD ["java", "-jar", "/app/app.jar"]