# Proyecto Spring Boot con Docker

Este es un proyecto Spring Boot que se ejecuta dentro de un contenedor Docker. Utiliza Make como herramienta de automatización para simplificar tareas comunes de desarrollo.

## Prerrequisitos

Antes de comenzar, asegúrate de tener instalados los siguientes requisitos:

- Docker: [Instrucciones de instalación](https://docs.docker.com/get-docker/)
- GNU Make: Si estás en un sistema Unix-like, Make generalmente viene preinstalado. Si no, puedes instalarlo utilizando tu gestor de paquetes de preferencia.
-  Base de datos PostgreSQL: Asegúrate de tener una base de datos PostgreSQL disponible para utilizarla con la aplicación. Si aún no tienes una instalada, puedes instalarla utilizando las instrucciones de la documentación oficial de PostgreSQL.

## Configuración del Proyecto

1. Clona este repositorio en tu máquina local:

    ```bash
    git clone https://github.com/clinic-appointment-finder/appointment-back.git
    ```

2. Navega al directorio del proyecto:

    ```bash
    cd appointment-back
    ```

## Uso

Para compilar y ejecutar el proyecto, sigue estos pasos:

1. Crea el archivo `application.properties`:

    ```bash
    make create-properties-file
    ```

     Este comando creará el archivo `application.properties` en el directorio `src/main/resources/` y establecerá la configuración predeterminada para una base de datos PostgreSQL. Deberás modificar este archivo según la configuración específica de tu base de datos.

    **Nota:** Asegúrate de modificar la conexión a la base de datos en el archivo `application.properties` según los detalles de tu base de datos PostgreSQL (nombre de usuario, contraseña, URL, etc.).
    </br>

2. Construye la imagen Docker:

    ```bash
    make docker-build
    ```
3. Ejecuta el contenedor Docker y entra en el contenedor:

    ```bash
    make docker-shell
    ```

4. Ejecuta la aplicación spring boot dentro del contenedor Docker:

    ```bash
    make run
    ```

Esto iniciará la aplicación Spring Boot dentro de un contenedor Docker. Puedes acceder a la documentación de la aplicación en `http://localhost:8080/webjars/swagger-ui/index.html`.

## Diagrama de la aplicación

![test](https://www.plantuml.com/plantuml/png/RL51QiCm4Bph5ODppXTAH9fIGp6cyQ5WJ51YEPZQMaLQKqp8O_g0lY8VgvIKDg7MGQ2PMNPcjF2i70zz9wnsdWm6FoZN4cRoC2WX3DKQYRIMMiEzDeocDRLsyADdtISu2g1ySXGOcUTLVDsgjsht7a53hC5aGSsq89oLv_EdXpeCvw-0Q5DVnWHGArPRvOEJd3pldVRNnK13NNIJzAtVM4UZtZ7zaa2f_bPjQWgakoux0IBSuYI43GxKFgRCKwpUYjLh9h5yIRDqAHFCHjrrz754J1OLQVQOhcKL9yLpKUHhpEyAA7yQh32VF-2Y43TyHASQ-0z5vNI0QUszlKMeBtphDm00)

