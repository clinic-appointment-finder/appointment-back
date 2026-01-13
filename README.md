# Appointment Finder Backend

Backend application for Clinic Appointment Finder, built with Spring Boot and designed to run in Docker. It manages task programming for analyzing doctor availability and integrates with external clinic APIs (specifically Indisa of Chile).

## Tech Stack

- **Java 17**
- **Spring Boot 3** (WebFlux, Data JPA, Cache)
- **PostgreSQL**
- **Docker & Docker Compose**
- **Mockito & JUnit 5** for Testing

## Prerequisites

Before starting, ensure you have:

- [Docker](https://docs.docker.com/get-docker/) installed.
- **Make** installed (usually pre-installed on Linux/Mac).
- A **PostgreSQL** database if running locally without Docker composition (optional, as dev setup can use Docker).

## Getting Started

1. **Clone the repository:**
    ```bash
    git clone https://github.com/clinic-appointment-finder/appointment-back.git
    cd appointment-back
    ```

2. **Configure usage properties:**
    Create the `application.properties` file with default values:
    ```bash
    make create-properties-file
    ```
    > **Note:** This creates `src/main/resources/application.properties`. Update the database connection details in this file to match your environment if you are not using the default setup.

## Running the Application

### Using Make & Docker (Recommended)

1. **Build the Docker image:**
    ```bash
    make docker-build
    ```

2. **Start the development shell:**
    This runs the container and drops you into a shell inside it.
    ```bash
    make docker-shell
    ```

3. **Run the application (inside the container):**
    ```bash
    make run
    ```
    The application will be available at `http://localhost:8080`.

### Running Tests

You can run unit tests using the newly added Make target, which executes them inside the Docker container to ensure environment consistency.

```bash
make docker-test
```

Or run them locally if you have Java 17 and Maven installed:
```bash
./mvnw test
```

## API Documentation

Once the application is running, Swagger UI documentation is available at:
- **Local:** [http://localhost:8080/webjars/swagger-ui/index.html](http://localhost:8080/webjars/swagger-ui/index.html)

## Application Architecture

![Architecture Diagram](https://www.plantuml.com/plantuml/png/RL51QiCm4Bph5ODppXTAH9fIGp6cyQ5WJ51YEPZQMaLQKqp8O_g0lY8VgvIKDg7MGQ2PMNPcjF2i70zz9wnsdWm6FoZN4cRoC2WX3DKQYRIMMiEzDeocDRLsyADdtISu2g1ySXGOcUTLVDsgjsht7a53hC5aGSsq89oLv_EdXpeCvw-0Q5DVnWHGArPRvOEJd3pldVRNnK13NNIJzAtVM4UZtZ7zaa2f_bPjQWgakoux0IBSuYI43GxKFgRCKwpUYjLh9h5yIRDqAHFCHjrrz754J1OLQVQOhcKL9yLpKUHhpEyAA7yQh32VF-2Y43TyHASQ-0z5vNI0QUszlKMeBtphDm00)

