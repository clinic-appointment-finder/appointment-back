
SHELL=/bin/bash
PACKAGE_NAME=appointment-back
PROJECT_FOLDER=.
GIT_DIR=$(shell pwd)

docker-build:
	docker build -t $(PACKAGE_NAME)-dev -f Dockerfile.dev  $(GIT_DIR)

docker-shell:
	docker run -it --rm -v $(GIT_DIR):/app --net proxy -p 8080:8080 -w /app/$(PROJECT_FOLDER) --name $(PACKAGE_NAME)-dev --entrypoint=/bin/bash $(PACKAGE_NAME)-dev

run:	
	#mvn compile exec:java -Dexec.cleanupDaemonThreads=false
	./mvnw spring-boot:run

# docker-debug:
# 	docker run -it --rm -v $(GIT_DIR):/app --net proxy -p 8080:8080 -p 5005:5005 -w /app/$(PROJECT_FOLDER) --name $(PACKAGE_NAME)-debug --entrypoint=/bin/bash  $(PACKAGE_NAME)-dev

# debug:	
# 	./mvnw spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
