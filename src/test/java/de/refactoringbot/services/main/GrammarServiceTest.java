package de.refactoringbot.services.main;

import java.util.ArrayList;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.refactoring.RefactoringImpl;
import de.refactoringbot.refactoring.RefactoringOperations;

public class GrammarServiceTest {

	private static Map<String, Class<? extends RefactoringImpl>> ruleToClassMapping;
	private final static String VALID_COMMENT_ADD_OVERRIDE = "@CorrectBotName ADD ANNOTATION Override";
	private final static String VALID_COMMENT_REORDER_MODIFIER = "@CorrectBotName REORDER MODIFIER";
	private final static String VALID_COMMENT_RENAME_METHOD = "@CorrectBotName RENAME METHOD TO newMethodName";
	private final static String VALID_COMMENT_REMOVE_PARAM = "@CorrectBotName REMOVE PARAMETER unusedParam";
	private final static String CORRECT_BOT_USERNAME = "CorrectBotName";

	private FileService fileService;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void setUpClass() {
		RefactoringOperations refactoringOperations = new RefactoringOperations();
		ruleToClassMapping = refactoringOperations.getRuleToClassMapping();
	}

	@Before
	public void initMocks() {
		fileService = Mockito.mock(FileService.class);
	}

	@Test
	public void testCommentToIssueMappingOverrideAnnotation() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_ADD_OVERRIDE);

		// assert
		String refactoringOperationKey = "Add Override Annotation";

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(ruleToClassMapping).containsKey(refactoringOperationKey);
		softAssertions.assertThat(botIssue.getRefactoringOperation()).isEqualTo(refactoringOperationKey);
		softAssertions.assertAll();
	}

	@Test
	public void testCommentToIssueMappingReorderModifiers() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_REORDER_MODIFIER);

		// assert
		String refactoringOperationKey = "Reorder Modifier";

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(ruleToClassMapping).containsKey(refactoringOperationKey);
		softAssertions.assertThat(botIssue.getRefactoringOperation()).isEqualTo(refactoringOperationKey);
		softAssertions.assertAll();
	}

	@Test
	public void testCommentToIssueMappingRenameMethod() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_RENAME_METHOD);

		// assert
		String refactoringOperationKey = "Rename Method";

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(ruleToClassMapping).containsKey(refactoringOperationKey);
		softAssertions.assertThat(botIssue.getRefactoringOperation()).isEqualTo(refactoringOperationKey);
		softAssertions.assertThat(botIssue.getRefactorString()).isEqualTo("newMethodName");
		softAssertions.assertAll();
	}

	@Test
	public void testCommentToIssueMappingRemoveParameter() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_REMOVE_PARAM);

		// assert
		String refactoringOperationKey = "Remove Parameter";

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(ruleToClassMapping).containsKey(refactoringOperationKey);
		softAssertions.assertThat(botIssue.getRefactoringOperation()).isEqualTo(refactoringOperationKey);
		softAssertions.assertThat(botIssue.getRefactorString()).isEqualTo("unusedParam");
		softAssertions.assertAll();
	}

	@Test
	public void testCheckComment() {
		GrammarService grammarService = new GrammarService(fileService);
		// Init gitconfig with bot username
		GitConfiguration gitConfig = new GitConfiguration();
		gitConfig.setBotName(CORRECT_BOT_USERNAME);
		
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(grammarService.checkComment(VALID_COMMENT_ADD_OVERRIDE, gitConfig)).isTrue();
		softAssertions.assertThat(grammarService.checkComment(VALID_COMMENT_ADD_OVERRIDE, gitConfig)).isTrue();
		softAssertions.assertThat(grammarService.checkComment(VALID_COMMENT_REORDER_MODIFIER, gitConfig)).isTrue();
		softAssertions.assertThat(grammarService.checkComment(VALID_COMMENT_RENAME_METHOD, gitConfig)).isTrue();
		softAssertions.assertThat(grammarService.checkComment(VALID_COMMENT_REMOVE_PARAM, gitConfig)).isTrue();

		softAssertions.assertThat(grammarService.checkComment("@IncorrectBotName ADD ANNOTATION", gitConfig)).isFalse();
		softAssertions.assertThat(grammarService.checkComment("BOT RENAME METHOD", gitConfig)).isFalse();
		softAssertions.assertThat(grammarService.checkComment("BOT, BOT, on the wall, who's the fairest of them all?", gitConfig))
				.isFalse();

		softAssertions.assertAll();
	}

	private BotIssue getIssueAfterMapping(String commentBody) throws Exception {
		// arrange
		Mockito.when(fileService.getAllJavaFiles(System.getProperty("user.dir"))).thenReturn(new ArrayList<>());
		GitConfiguration gitConfig = new GitConfiguration();
		GrammarService grammarService = new GrammarService(fileService);
		BotPullRequestComment comment = new BotPullRequestComment();
		comment.setCommentID(0);
		comment.setUsername("randomuser");
		comment.setFilepath("");
		comment.setPosition(0);
		comment.setCommentBody(commentBody);
		String repoFolder = System.getProperty("user.dir");
		gitConfig.setRepoFolder(repoFolder);

		// act
		BotIssue botIssue = new BotIssue();
		botIssue = grammarService.createIssueFromComment(comment, gitConfig);

		return botIssue;
	}

}
