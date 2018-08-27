package refactoring;

public class Config {
	
	int maxAnzahlOpenPullRequests = 1;
	String sonarCloudProjectName = "Test:Test:master";
	String fileLocation = "c://Users/Timo/Test/git/Calculator/";
	String githubProject = "Refactoring-Bot/RefactoringTest";

	public int getMaxAnzahlOpenPullRequests() {
		return maxAnzahlOpenPullRequests;
	}
	
	public void setMaxAnzahlOpenPullRequests(int maxAnzahlOpenPullRequests) {
		this.maxAnzahlOpenPullRequests = maxAnzahlOpenPullRequests;
	}

	public String getSonarCloudProjectName() {
		return sonarCloudProjectName;
	}

	public void setSonarCloudProjectName(String sonarCloudProjectName) {
		this.sonarCloudProjectName = sonarCloudProjectName;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public String getGithubProject() {
		return githubProject;
	}

	public void setGithubProject(String githubProject) {
		this.githubProject = githubProject;
	}
	
}
