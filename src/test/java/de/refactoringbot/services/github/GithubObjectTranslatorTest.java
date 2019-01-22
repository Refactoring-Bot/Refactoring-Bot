package de.refactoringbot.services.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import de.refactoringbot.model.configuration.AnalysisProvider;
import de.refactoringbot.model.configuration.FileHoster;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.github.pullrequestcomment.ReplyComment;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.services.main.GitService;

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

		GithubObjectTranslator githubObjectTranslator = new GithubObjectTranslator(null, null, null);
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
		GithubObjectTranslator githubObjectTranslator = new GithubObjectTranslator(null, modelMapper, null);
		GitConfiguration gitConfiguration = githubObjectTranslator.createConfiguration(configurationDto);

		GitConfiguration expectedGitConfiguration = createExpectedGitConfiguration();

		assertThat(gitConfiguration).isEqualToComparingFieldByField(expectedGitConfiguration);
	}

	@Test
	public void getAbsoluteLineNumberOfPullRequestComment() {
		// The following diffHunks are examples from PR #36 and returned by the API
		// exactly like this.
		// https://api.github.com/repos/Refactoring-Bot/Refactoring-Bot/pulls/36/comments
		String diffHunk0 = "@@ -115,30 +112,30 @@ private void testRemoveParameter(int lineNumberOfMethodWithParameterToBeRemoved,\n \t\tMethodDeclaration refactoredMethod = getMethodByName(methodName, cu);\n \n \t\t// assert that parameter has been removed from the target method\n-\t\tassertNotNull(refactoredMethod);\n-\t\tassertFalse(refactoredMethod.getParameterByName(parameterName).isPresent());\n+\t\tassertThat(refactoredMethod).isNotNull();\n+\t\tassertThat(refactoredMethod.getParameterByName(parameterName).isPresent()).isFalse();\n \n \t\t// assert that parameter has also been removed from the javadoc\n \t\tList<JavadocBlockTag> javadocBlockTags = refactoredMethod.getJavadoc().get().getBlockTags();\n \t\tfor (JavadocBlockTag javadocBlockTag : javadocBlockTags) {\n-\t\t\tassertFalse(javadocBlockTag.getTagName().equals(\"param\")\n-\t\t\t\t\t&& javadocBlockTag.getName().get().equals(parameterName));\n+\t\t\tassertThat(javadocBlockTag.getTagName().equals(\"param\")";
		Integer expectedCommentPosition0 = 121;
		String diffHunk1 = "@@ -91,9 +88,9 @@ private void testRemoveParameter(int lineNumberOfMethodWithParameterToBeRemoved,\n \t\t\t\tremoveParameterTestClass.getLineNumberOfDummyMethod(0, 0, 0), cuOriginalFile);\n \t\tMethodDeclaration originalCallerMethod = RefactoringHelper\n \t\t\t\t.getMethodByLineNumberOfMethodName(removeParameterTestClass.getLineNumberOfCaller(), cuOriginalFile);\n-\t\tassertNotNull(originalMethod);\n-\t\tassertNotNull(originalDummyMethod);\n-\t\tassertNotNull(originalCallerMethod);\n+\t\tassertThat(originalMethod).isNotNull();";
		Integer expectedCommentPosition1 = 91;
		String diffHunk2 = "@@ -53,7 +52,7 @@ public void testRenameMethod() throws Exception {\n \t\tString originalSecondMethodName = RefactoringHelper\n \t\t\t\t.getMethodByLineNumberOfMethodName(lineNumberOfSecondMethodNotToBeRenamed, cuOriginalFile)\n \t\t\t\t.getNameAsString();\n-\t\tassertEquals(originalMethodName, originalSecondMethodName);\n+\t\tassertThat(originalSecondMethodName).isEqualTo(originalMethodName);";
		Integer expectedCommentPosition2 = 55;
		String diffHunk3 = "@@ -34,18 +34,18 @@ public void testFindJavaRoots() throws IOException {\n \n \t\t// act\n \t\tList<String> javaRoots = fileService.findJavaRoots(allJavaFiles);\n-\t\n+\n \t\t// assert\n-\t\tassertTrue(javaRoots.size() > 0);\n-\t\tassertTrue(javaRoots.contains(getAbsoluteJavaRootPathOfThis()));\n+\t\tassertThat(javaRoots).size().isGreaterThan(0);\n+\t\tassertThat(javaRoots).contains(getAbsoluteJavaRootPathOfThis());";
		Integer expectedCommentPosition3 = 40;
		String diffHunk4 = "@@ -53,9 +49,9 @@ public void testCommentToIssueMappingOverrideAnnotation() throws Exception {\n \n \t\t// assert\n \t\tString refactoringOperationKey = \"Add Override Annotation\";\n-\t\tassertTrue(ruleToClassMapping.containsKey(refactoringOperationKey));\n-\t\tassertEquals(refactoringOperationKey, botIssue.getRefactoringOperation());\n-\t\tassertEquals(Integer.valueOf(5), botIssue.getLine());\n+\t\tassertThat(ruleToClassMapping).containsKey(refactoringOperationKey);";
		Integer expectedCommentPosition4 = 52;

		GitService gitService = new GitService();

		Integer actual0 = gitService.translateDiffHunkToPosition(diffHunk0);
		Integer actual1 = gitService.translateDiffHunkToPosition(diffHunk1);
		Integer actual2 = gitService.translateDiffHunkToPosition(diffHunk2);
		Integer actual3 = gitService.translateDiffHunkToPosition(diffHunk3);
		Integer actual4 = gitService.translateDiffHunkToPosition(diffHunk4);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(actual0).isEqualTo(expectedCommentPosition0);
		softAssertions.assertThat(actual1).isEqualTo(expectedCommentPosition1);
		softAssertions.assertThat(actual2).isEqualTo(expectedCommentPosition2);
		softAssertions.assertThat(actual3).isEqualTo(expectedCommentPosition3);
		softAssertions.assertThat(actual4).isEqualTo(expectedCommentPosition4);
		softAssertions.assertAll();
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
