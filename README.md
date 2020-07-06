# Refactoring-Bot

[![Build Status](https://github.com/Refactoring-Bot/Refactoring-Bot/workflows/build/badge.svg)](https://github.com/Refactoring-Bot/Refactoring-Bot/actions)
[![Sonarcloud Dashboard](https://sonarcloud.io/api/project_badges/measure?project=de.refactoringBot%3ARefactoringBot&metric=alert_status)](https://sonarcloud.io/dashboard?id=de.refactoringBot%3ARefactoringBot) 

Implementation of a bot that performs automatic refactorings based on the results of static code analysis. The changes are made available to the developers as pull requests for easy review. It is possible to interact with the bot through natural language line comments in the proposed pull requests.

## Repositories

- [Refactoring-Bot/Refactoring-Bot](https://github.com/Refactoring-Bot/Refactoring-Bot)
contains the backend functionality for the interaction between bot, web-based hosting service for version control and static analysis tools, as well as the technical implementation of the automatic refactoring of code smells. It is a Spring Boot application providing a Swagger UI to interact with its REST API resources.
- [Refactoring-Bot/Refactoring-Bot-UI](https://github.com/Refactoring-Bot/Refactoring-Bot-UI)
is a web frontend (under development) to conveniently configure and manage the Refactoring-Bot.
- [Refactoring-Bot/Bot-Playground](https://github.com/Refactoring-Bot/Bot-Playground)
contains exactly one example per code smell that the bot can (soon) automatically fix. At present, these are exclusively findings of SonarQube.
- [Refactoring-Bot/Docker](https://github.com/Refactoring-Bot/Docker)
contains everything for installing the Refactoring-Bot via Docker.

## Developer Usage Instructions
Before you can use the bot locally, the following steps need to be executed.

1. The bot needs access to a MySQL DB instance to save its data. Use an existing one, download MySQL for your OS, or create a docker container via `docker run --name refactoring-bot-db -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:latest`.
2. In this MySQL instance, you need to create a schema with the name `refactoringbot_db` and make sure the user for the bot has access rights to this schema.
3. Copy the configuration file `src/main/resources/application_example.yml` and rename it to `src/main/resources/application.yml`. In this new file, potentially change the `datasource` attributes depending on your MySQL instance.
4. Execute the command `mvn install` to create the executable JAR file for the bot.
5. Run the created JAR file via `java -jar ./target/RefactoringBot-0.0.1-SNAPSHOT.jar`. The API should now be available at `http://localhost:8808` and the SwaggerUI will open in the browser.

**Docker Support:** Please refer to our [Docker Repository](https://github.com/Refactoring-Bot/Docker) for detailed usage instructions with `docker` or `docker-compose`.

## Contributing

We are happy about any kind of contribution which can improve the Refactoring-Bot. Feature requests, bug reports and questions can be created informally via [Issues](https://github.com/Refactoring-Bot/Refactoring-Bot/issues). To contribute a change to the source code, just have a look at our [contributing guidelines](https://github.com/Refactoring-Bot/Refactoring-Bot/blob/master/CONTRIBUTING.md). Please make sure you have created an issue on your change beforehand. This way, we try to avoid unnecessary effort or multiple people doing the same revision at the same time.
