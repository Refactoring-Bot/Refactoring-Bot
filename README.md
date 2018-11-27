# Refactoring-Bot

Implementation of a bot, that does automatic refactorings based on the result of a static code analysis from analysis-services like SonarCloud or from comments within pull-requests of filehosting-services like GitHub. After a successful refactoring the bot creates/updates a pull-request with the changes.

## Requirements for using this bot

1. MySQL [sql](https://www.mysql.com/de/products/community/) must be installed, configured and running.

## Requirements for the project you would like to refactor with comments from pull-requests

1. Project is in a public filehoster repository (only GitHub is supported at the moment).
2. User account of the specific filehoster with an API token (you can create yourself a bot user and generate a token for hin on GitHub for example).

## Added requirements for refactoring with analysis-services like SonarCube
1. Project needs to be publicaly hosted on a analysis-service (only SonarCube is supported at the moment).
2. Unique ProjectKey of the hosted project for the API access of the analysis-service.

## How to use this bot

1. Install MySQL and configure it. (Create a MySQL Connection and a user who can access it)
2. In your IDE, open the application.yml file and fill the example values with your own.
3. Run the project as as Spring-Project.
4. Either your browser will open the Swagger-UI (automated GUI) automatically or visit 'http://localhost:YOUR_PORT/swagger-ui.html'.
5. Go to the Configuration-Controller and create an configuration.
6. If successful, go to the Refactoring-Controller and perform refactorings with comments from pull-requests of GitHub or from a static code analysis of SonarCube. You will have to chose the configuration you want to use with the Configuration-ID.