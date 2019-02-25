FROM java:8-alpine
LABEL maintainer="Marvin Wyrich <mail@marvin-wyrich.de>"

# copy the created jar file to the image (make sure it exists ==> `mvn clean install`)
COPY target/RefactoringBot-0.0.1-SNAPSHOT.jar /opt/refactoring-bot/RefactoringBot-0.0.1-SNAPSHOT.jar

ENV LOCAL_DIR=/opt/refactoring-bot/repos
CMD ["java","-jar","/opt/refactoring-bot/RefactoringBot-0.0.1-SNAPSHOT.jar"]