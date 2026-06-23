SHELL=/bin/bash
PACKAGE_NAME=appointment-back
PROJECT_FOLDER=.
GIT_DIR=$(shell pwd)
DOCKER_BUILDKIT ?= 1

docker-build:
	DOCKER_BUILDKIT=$(DOCKER_BUILDKIT) docker build -t $(PACKAGE_NAME)-dev -f Dockerfile.dev $(GIT_DIR)

docker-shell:
	docker run -it --rm -v $(GIT_DIR):/app --net proxy -p 8080:8080 -w /app/$(PROJECT_FOLDER) --name $(PACKAGE_NAME)-dev --entrypoint=/bin/bash $(PACKAGE_NAME)-dev

docker-test:
	docker run --rm -v $(GIT_DIR):/app --net proxy -w /app/$(PROJECT_FOLDER) --name $(PACKAGE_NAME)-test $(PACKAGE_NAME)-dev ./mvnw test

run:	
	#mvn compile exec:java -Dexec.cleanupDaemonThreads=false
	./mvnw spring-boot:run

FILE_PATH = src/main/resources/application.properties

create-properties-file:
	@echo "FILE_PATH: $(FILE_PATH)"
	@if [ -f "$(FILE_PATH)" ]; then \
	    echo "El archivo $(FILE_PATH) ya existe"; \
	else \
	    echo "Creando el archivo $(FILE_PATH)"; \
        echo "spring.datasource.url=jdbc:postgresql://<<<REPLACE_HOST_HERE>>>:5432/<<<REPLACE_DATABASE_HERE>>>" > $(FILE_PATH); \
        echo "spring.datasource.username=<<<REPLACE_USERNAME_HERE>>>" >> $(FILE_PATH); \
        echo "spring.datasource.password=<<<REPLACE_PASSWORD_HERE>>>" >> $(FILE_PATH); \
        echo "spring.datasource.driver-class-name=org.postgresql.Driver" >> $(FILE_PATH); \
        echo "spring.jpa.properties.hibernate.default_schema=appointment_doctor" >> $(FILE_PATH); \
        echo "spring.jpa.database=POSTGRESQL" >> $(FILE_PATH); \
		echo "#MODO DEV print sql, false in production" >> $(FILE_PATH); \
        echo "spring.jpa.show-sql=true" >> $(FILE_PATH); \
        echo "spring.jpa.hibernate.ddl-auto=update" >> $(FILE_PATH); \
        echo "management.endpoints.web.exposure.include=*" >> $(FILE_PATH); \
        echo "" >> $(FILE_PATH); \
        echo "logging.level.cl.hcs.finder.appointmentback.service=info" >> $(FILE_PATH); \
        echo "" >> $(FILE_PATH); \
        echo "swagger.openapi.dev-url=http://localhost:8080" >> $(FILE_PATH); \
        echo "swagger.openapi.prod-url=https://clinic-appointment.ultra-neo.com" >> $(FILE_PATH); \
        echo "swagger.openapi.url.portfolio=https://portfolio.ultra-neo.com" >> $(FILE_PATH); \
        echo "" >> $(FILE_PATH); \
        echo "indisa.app.url=https://agenda.eniax.cl" >> $(FILE_PATH); \
        echo "indisa.app.url.referer=https://agenda.eniax.cl/consulta_indisa" >> $(FILE_PATH); \
        echo "indisa.app.url.path.office=/api/v1/search" >> $(FILE_PATH); \
        echo "indisa.app.url.path.speciality=\$${indisa.app.url.path.office}" >> $(FILE_PATH); \
        echo "indisa.app.url.path.doctors=\$${indisa.app.url.path.office}" >> $(FILE_PATH); \
        echo "indisa.app.url.path.prevision=/api/v1/medical_insurances/consulta_indisa" >> $(FILE_PATH); \
        echo "indisa.app.url.path.schedule=/api/v1/enroll_patient/consulta_indisa" >> $(FILE_PATH); \
        echo "indisa.app.url.path=\$${indisa.app.url}/consulta_indisa" >> $(FILE_PATH); \
        echo "indisa.app.url.path.calendar=/api/v1/create_calendar" >> $(FILE_PATH); \
        echo "indisa.app.calendar.body=spec_id=<<<PUT_VALUE_HERE>>>&doc_id=<<<PUT_VALUE_HERE>>>&source=ajax&office_id=<<<PUT_VALUE_HERE>>>&month=0&year=0" >> $(FILE_PATH); \
        echo "indisa.app.rut=11.111.111-1" >> $(FILE_PATH); \
        echo "indisa.app.agenda.id=67f9519ec9bb0708cbd1a7e6" >> $(FILE_PATH); \
        echo "indisa.app.body=spec_id=&doc_id=&office_id=&source=browser&isapre=&patient_rut=\$${indisa.app.rut}" >> $(FILE_PATH); \
        echo "indisa.app.url.path.doctors.image.default=https://agenda.eniax.cl/static/images/agenda/Medico_2.png" >> $(FILE_PATH); \
	fi

.PHONY: create-properties-file
