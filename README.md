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

## Docker Support
Please refer to our [Docker Repository](https://github.com/Refactoring-Bot/Docker) for detailed usage instructions with `docker` or `docker-compose`.