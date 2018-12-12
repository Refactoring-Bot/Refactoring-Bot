package de.refactoringBot.model.configuration;

public class GitConfigurationDTO {

	private String repoName;
	private String repoOwner;
	private String repoService;
	
	private String botName;
	private String botPassword;
	private String botEmail;
	private String botToken;
	private String analysisService;
	private String analysisServiceProjectKey;
	private Integer maxAmountRequests;
	
	public String getRepoName() {
		return repoName;
	}
	
	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getRepoOwner() {
		return repoOwner;
	}

	public void setRepoOwner(String repoOwner) {
		this.repoOwner = repoOwner;
	}

	public String getRepoService() {
		return repoService;
	}

	public void setRepoService(String repoService) {
		this.repoService = repoService;
	}

	public String getBotName() {
		return botName;
	}

	public void setBotName(String botName) {
		this.botName = botName;
	}

	public String getBotPassword() {
		return botPassword;
	}

	public void setBotPassword(String botPassword) {
		this.botPassword = botPassword;
	}

	public String getBotToken() {
		return botToken;
	}

	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	public Integer getMaxAmountRequests() {
		return maxAmountRequests;
	}

	public void setMaxAmountRequests(Integer maxAmountRequests) {
		this.maxAmountRequests = maxAmountRequests;
	}

	public String getAnalysisService() {
		return analysisService;
	}

	public void setAnalysisService(String analysisService) {
		this.analysisService = analysisService;
	}

	public String getAnalysisServiceProjectKey() {
		return analysisServiceProjectKey;
	}

	public void setAnalysisServiceProjectKey(String analysisServiceProjectKey) {
		this.analysisServiceProjectKey = analysisServiceProjectKey;
	}

	public String getBotEmail() {
		return botEmail;
	}

	public void setBotEmail(String botEmail) {
		this.botEmail = botEmail;
	}
	
}
