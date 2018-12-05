# Refactoring-Bot
Implementation of a bot, that does automatic refactorings based on the result of a static code analysis from analysis-services like SonarCloud or from comments within pull-requests of filehosting-services like GitHub. After a successful refactoring the bot creates/updates a pull-request with the changes.

## Requirements for using this bot
1. [MySQL](https://www.mysql.com/de/products/community/) must be installed, configured and running.
2. [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) must be installed.

## Requirements for the project you would like to refactor with comments from pull-requests
1. Project is in a public filehoster repository (only GitHub is supported at the moment).
2. User account of the specific filehoster with an API token (you can create yourself a bot user and generate a token for hin on GitHub for example).

## Requirements for refactoring with analysis-services like SonarCube
1. Project needs to be publicaly hosted on a analysis-service (only sonarcloud is supported at the moment).
2. Unique ProjectKey of the hosted project for the API access of the analysis-service.

## How to use this bot
1. Install MySQL and configure it. (Create a MySQL Connection and a user who can access it)
2. Import the project as maven project to your IDE.
3. In your IDE, copy the application_example.yml file, rename it to application.yml and fill the example values with your own.
4. Run the project as Spring-Project. If you use Eclipse, *run as maven build...* and add ``spring-boot:run`` to the goals.
5. Either your browser will open the Swagger-UI (automated GUI) automatically or visit 'http://localhost:YOUR_PORT/swagger-ui.html'.
6. Go to the Configuration-Controller and create a configuration.
7. If successful, go to the Refactoring-Controller and perform refactorings with comments from pull-requests of GitHub or from a static code analysis of SonarCube. You will have to chose the configuration you want to use with the Configuration-ID.
