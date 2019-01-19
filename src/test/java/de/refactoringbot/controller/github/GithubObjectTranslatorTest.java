package de.refactoringbot.controller.github;

import static org.assertj.core.api.Assertions.assertThat;

import de.refactoringbot.model.configuration.AnalysisProvider;
import de.refactoringbot.model.configuration.FileHoster;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.github.pullrequestcomment.ReplyComment;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;

public class GithubObjectTranslatorTest {

	private final String repositoryOwner = "testRepositoryOwner";
	private final String repositoryName = "testRepositoryName";
	private final String botName = "testBotName";
	private final AnalysisProvider analysisService = AnalysisProvider.sonarqube;
	private final String botEmail = "testBotEmail";
	private final String analysisKey = "testAnalysisKey";
	private final String botToken = "testBotToken";
	private final FileHoster repositoryService = FileHoster.github;
	private final Integer maxRequests = 456;

	@Test
	public void createFailureReply() {
		BotPullRequestComment comment = new BotPullRequestComment();
		Integer commentId = 123;
		comment.setCommentID(commentId);
		String errorMessage = "test error message";

		GithubObjectTranslator githubObjectTranslator = new GithubObjectTranslator(null, null);
		ReplyComment failureReply = githubObjectTranslator.createFailureReply(comment, errorMessage);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(failureReply.getIn_reply_to()).isEqualTo(commentId);
		softAssertions.assertThat(failureReply.getBody()).isEqualTo(errorMessage);
		softAssertions.assertAll();
	}

	@Test
	public void createConfiguration() {
		GitConfigurationDTO configurationDto = createGitConfigurationDto();

		ModelMapper modelMapper = new ModelMapper();
		GithubObjectTranslator githubObjectTranslator = new GithubObjectTranslator(null, modelMapper);
		GitConfiguration gitConfiguration = githubObjectTranslator.createConfiguration(configurationDto);

		GitConfiguration expectedGitConfiguration = createExpectedGitConfiguration();

		assertThat(gitConfiguration).isEqualToComparingFieldByField(expectedGitConfiguration);
	}

	private GitConfigurationDTO createGitConfigurationDto() {
		GitConfigurationDTO configurationDto = new GitConfigurationDTO();
		configurationDto.setAnalysisService(analysisService);
		configurationDto.setAnalysisServiceProjectKey(analysisKey);
		configurationDto.setBotName(botName);
		configurationDto.setBotEmail(botEmail);
		configurationDto.setBotToken(botToken);
		configurationDto.setRepoOwner(repositoryOwner);
		configurationDto.setRepoName(repositoryName);
		configurationDto.setRepoService(repositoryService);
		configurationDto.setMaxAmountRequests(maxRequests);
		return configurationDto;
	}

	private GitConfiguration createExpectedGitConfiguration() {
		GitConfiguration gitConfiguration = new GitConfiguration();
		gitConfiguration.setAnalysisService(analysisService);
		gitConfiguration.setAnalysisServiceProjectKey(analysisKey);
		gitConfiguration.setBotName(botName);
		gitConfiguration.setBotEmail(botEmail);
		gitConfiguration.setBotToken(botToken);
		gitConfiguration.setRepoName(repositoryName);
		gitConfiguration.setRepoOwner(repositoryOwner);
		gitConfiguration.setRepoService(repositoryService);
		gitConfiguration.setMaxAmountRequests(maxRequests);

		gitConfiguration.setRepoApiLink("https://api.github.com/repos/" + repositoryOwner + "/" + repositoryName);
		gitConfiguration.setRepoGitLink("https://github.com/" + repositoryOwner + "/" + repositoryName + ".git");
		gitConfiguration.setForkApiLink("https://api.github.com/repos/" + botName + "/" + repositoryName);
		gitConfiguration.setForkGitLink("https://github.com/" + botName + "/" + repositoryName + ".git");

		return gitConfiguration;
	}
}
