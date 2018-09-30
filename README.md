# Refactoring-Bot

Implementation of a bot, that does automatic refactorings based on the result of a static code analysis from sonarcloud. After the refactoring the bot creates a pull-request with the changes.
## Requirements for using this bot

1. GitHub [hub](https://github.com/github/hub) must be installed and a user for hub must be configurated as described [here](https://hub.github.com/hub.1.html).
2. [Git](https://git-scm.com/) must be installed.
3. [Apache Maven](https://maven.apache.org/) must be installed.

## Requirements for the project 

1. Project is in a public Github repository.
2. Results from the static code analysis are on sonarcloud with public access.

## How to use this bot

1. Create properties file for the project you want to refactor. A sample file can be found [here](https://github.com/Refactoring-Bot/RefactoringScripts).
2. Get the runBot.sh Script from [here](https://github.com/Refactoring-Bot/RefactoringScripts).
3. Clone the bot repository and the code from the repository you want to refactor.
4. Adjust the properties file and the runBot script for the project you want to refactor. 

## How to implement new Refactorings

Create new class for the refactoring and implement it in this class. It should implement the interface "Refactoring". Add the sonarcloud rule to the "RefactoringRules" class and add the rule to the switch-case block in the main method from the "RefactoringBot" class.
