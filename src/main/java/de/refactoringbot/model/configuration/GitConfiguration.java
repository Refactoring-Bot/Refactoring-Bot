package de.refactoringbot.model.configuration;

import javax.persistence.*;

@Entity
@Table(name = "GIT_CONFIGURATIONS")
public class GitConfiguration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long configurationId;

	private String repoName;
	private String repoOwner;
	private String repoApiLink;
	private String repoGitLink;

	@Enumerated(EnumType.STRING)
	private FileHoster repoService;

	private String repoFolder;
	private String srcFolder;

	private String botName;
	private String botEmail;
	private String botToken;
	private String forkApiLink;
	private String forkGitLink;
	private String analysisService;
	private String analysisServiceProjectKey;
	private Integer maxAmountRequests;

	public Long getConfigurationId() {
		return configurationId;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getRepoApiLink() {
		return repoApiLink;
	}

	public void setRepoApiLink(String repoApiLink) {
		this.repoApiLink = repoApiLink;
	}

	public String getRepoGitLink() {
		return repoGitLink;
	}

	public void setRepoGitLink(String repoGitLink) {
		this.repoGitLink = repoGitLink;
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

	public String getForkApiLink() {
		return forkApiLink;
	}

	public void setForkApiLink(String forkApiLink) {
		this.forkApiLink = forkApiLink;
	}

	public String getForkGitLink() {
		return forkGitLink;
	}

	public void setForkGitLink(String forkGitLink) {
		this.forkGitLink = forkGitLink;
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

	public String getRepoFolder() {
		return repoFolder;
	}

	public void setRepoFolder(String repoFolder) {
		this.repoFolder = repoFolder;
	}

	public String getSrcFolder() {
		return srcFolder;
	}

	public void setSrcFolder(String srcFolder) {
		this.srcFolder = srcFolder;
	}

}
