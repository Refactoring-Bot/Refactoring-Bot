package de.refactoringbot.model.configuration;

public class GitConfigurationDTO {

	private String repoName;
	private String repoOwner;
	private FileHoster repoService;
	private String filehosterApiLink;

	private Long gitUserId;
	private String botName;
	private String botEmail;
	private String botToken;
	private AnalysisProvider analysisService;
	private String analysisServiceProjectKey;
	private String analysisServiceApiLink;
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

	public FileHoster getRepoService() {
		return repoService;
	}

	public void setRepoService(FileHoster repoService) {
		this.repoService = repoService;
	}

	public Long getGitUserId() {
		return gitUserId;
	}

	public void setGitUserId(Long gitUserId) {
		this.gitUserId = gitUserId;
	}

	public String getBotName() {
		return botName;
	}

	public void setBotName(String botName) {
		this.botName = botName;
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

	public AnalysisProvider getAnalysisService() {
		return analysisService;
	}

	public void setAnalysisService(AnalysisProvider analysisService) {
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

	public String getAnalysisServiceApiLink() {
		return analysisServiceApiLink;
	}

	public void setAnalysisServiceApiLink(String analysisServiceApiLink) {
		this.analysisServiceApiLink = analysisServiceApiLink;
	}

	public String getFilehosterApiLink() {
		return filehosterApiLink;
	}

	public void setFilehosterApiLink(String filehosterApiLink) {
		this.filehosterApiLink = filehosterApiLink;
	}

}
