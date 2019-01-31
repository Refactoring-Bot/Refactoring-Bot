# Refactoring-Bot

[![Build Status](https://travis-ci.org/Refactoring-Bot/Refactoring-Bot.svg?branch=master)](https://travis-ci.org/Refactoring-Bot/Refactoring-Bot) 
[![Sonarcloud Dashboard](https://sonarcloud.io/api/project_badges/measure?project=de.refactoringBot%3ARefactoringBot&metric=alert_status)](https://sonarcloud.io/dashboard?id=de.refactoringBot%3ARefactoringBot) 

Implementation of a bot that performs automatic refactorings based on the results of static code analysis or comments in pull requests. The changes are made available to the developers as pull requests for easy review.

Learn more about the bot and how to use it in the [wiki](https://github.com/Refactoring-Bot/Refactoring-Bot/wiki).

## Developer Usage Instructions

Before you can use the bot locally, the following steps need to be executed.

1. The bot needs access to a MySQL DB instance to save its data. Use an existing one, download MySQL for your OS, or create a docker container via `docker run --name refactoring-bot-db -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:latest`.
2. In this MySQL instance, you need to create a schema with the name `refactoringbot_db` and make sure the user for the bot has access rights to this schema.
3. Copy the configuration file `src/main/resources/application_example.yml` and rename it to `src/main/resources/application.yml`. In this new file, potentially change the `datasource` attributes depending on your MySQL instance.
4. Execute the command `mvn install` to create the executable JAR file for the bot.
5. Run the created JAR file via `java -jar ./target/RefactoringBot-0.0.1-SNAPSHOT.jar`. The API should now be available at `http://localhost:8808` and the SwaggerUI will open in the browser.

## Docker Container

Use the following instructions to handle a local Docker image and container of the Refactoring-Bot.

```bash
# Make sure the jar file has been built and is available in the `target` directory
# If not execute the following command
maven clean install

# Build image from Dockerfile
docker build -t refactoring-bot .

# Create container from image, exposing port 8808, and setting the host for the DB via an ENV variable
# (if the DB is also in a Docker container, use `docker inspect <container-name>` to find out its IP)
docker create --name refactoring-bot \
    -p 8808:8808 \
    -e DATABASE_HOST=172.17.0.2 \
    refactoring-bot:latest

# Start container
docker start refactoring-bot

# Optional: open interactive shell to connect to a running container (yes, `ash` is the shell in alpine, this is no typo)
docker exec -it refactoring-bot ash
```