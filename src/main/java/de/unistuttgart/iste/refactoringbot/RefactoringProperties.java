package de.unistuttgart.iste.refactoringbot;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Class for representing the properties file containing refactoring configurations
 */
public class RefactoringProperties {

    /**
     * private constructor to hide public one
     */
    private RefactoringProperties() {}

    private String sonarCloudProjectKey;
    private String targetProjectFileLocation;
    private String targetProjectGitHubPath;
    private String bashLocation;
    private String pullRequestScriptFileLocation;
    private int maxNumberOfOpenPullRequests;
    private String gitHubLoginName;
    private String processedSonarIssuesFileLocation;

    /**
     * @param pathToPropertiesFile path to the properties file
     * @return a new RefactoringProperties object representing the properties file at the given path
     * @throws IOException
     */
    public static RefactoringProperties createFromPropertiesFile(String pathToPropertiesFile) throws IOException {
        Properties properties = new Properties();
        FileReader in = new FileReader(pathToPropertiesFile);
        properties.load(in);

        // TODO rename variables in properties file to match attributes in this class
        RefactoringProperties result = new RefactoringProperties();
        result.sonarCloudProjectKey = properties.getProperty("sonarCloudProjectName");
        result.targetProjectFileLocation = properties.getProperty("fileLocation");
        result.targetProjectGitHubPath = properties.getProperty("gitHubProject");
        result.bashLocation = properties.getProperty("bashLocation");
        result.pullRequestScriptFileLocation = properties.getProperty("pullRequestScriptLocation");
        result.maxNumberOfOpenPullRequests = Integer.valueOf(properties.getProperty("maxNumberOfOpenPullRequests"));
        result.gitHubLoginName = properties.getProperty("gitHubLoginName");
        result.processedSonarIssuesFileLocation = properties.getProperty("listOfDoneIssuesLocation");

        return result;
    }

    /**
     * @return e.g. 'OrganizationName/ProjectName'
     */
    public String getTargetProjectGitHubPath() {
        return targetProjectGitHubPath;
    }

    /**
     * @return name of the project on GitHub
     */
    public String getTargetProjectGitHubName() {
        return this.targetProjectGitHubPath.substring(
                this.targetProjectGitHubPath.indexOf("/") + 1, this.targetProjectGitHubPath.length());
    }

    /**
     * @return name of the repo owner on GitHub
     */
    public String getTargetProjectGitHubOwner() {
        return this.targetProjectGitHubPath.substring(0, this.targetProjectGitHubPath.indexOf("/"));
    }

    /**
     * @return sonar project key
     */
    public String getSonarCloudProjectKey() {
        return sonarCloudProjectKey;
    }

    /**
     * @return path to the local git project which should be refactored
     */
    public String getTargetProjectFileLocation() {
        return targetProjectFileLocation;
    }

    /**
     * @return path to the bash to run the pull request script with
     */
    public String getBashLocation() {
        return bashLocation;
    }

    /**
     * @return path to the pull request script file
     */
    public String getPullRequestScriptFileLocation() {
        return pullRequestScriptFileLocation;
    }

    /**
     * @return maximum number of simultaneously open pull requests by the bot user for the target project
     */
    public int getMaxNumberOfOpenPullRequests() {
        return maxNumberOfOpenPullRequests;
    }

    /**
     * @return name of the bot user on GitHub
     */
    public String getGitHubLoginName() {
        return gitHubLoginName;
    }

    /**
     * @return  path to the file with the list of already processed sonar issues
     */
    public String getProcessedSonarIssuesFileLocation() {
        return processedSonarIssuesFileLocation;
    }

    /**
     * @return name of the file with the list of already processed sonar issues
     */
    public String getProcessedSonarIssuesFileName() {
        return "IssuesDone" + this.getTargetProjectGitHubName() + ".txt";
    }


}
