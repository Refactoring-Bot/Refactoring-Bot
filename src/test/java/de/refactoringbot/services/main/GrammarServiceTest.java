package de.refactoringbot.services.main;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Map;

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
	private final static String VALID_COMMENT_ADD_OVERRIDE = "BOT ADD ANNOTATION Override LINE 5";
	private final static String VALID_COMMENT_REORDER_MODIFIER = "BOT REORDER MODIFIER LINE 10";
	private final static String VALID_COMMENT_RENAME_METHOD = "BOT RENAME METHOD LINE 15 TO newMethodName";
	private final static String VALID_COMMENT_REMOVE_PARAM = "BOT REMOVE PARAMETER LINE 20 NAME unusedParam";

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
		assertThat(ruleToClassMapping).containsKey(refactoringOperationKey);
		assertThat(botIssue.getRefactoringOperation()).isEqualTo(refactoringOperationKey);
		assertThat(botIssue.getLine()).isEqualTo(5);
	}

	@Test
	public void testCommentToIssueMappingReorderModifiers() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_REORDER_MODIFIER);

		// assert
		String refactoringOperationKey = "Reorder Modifier";
		assertThat(ruleToClassMapping).containsKey(refactoringOperationKey);
		assertThat(botIssue.getRefactoringOperation()).isEqualTo(refactoringOperationKey);
		assertThat(botIssue.getLine()).isEqualTo(10);
	}

	@Test
	public void testCommentToIssueMappingRenameMethod() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_RENAME_METHOD);

		// assert
		String refactoringOperationKey = "Rename Method";
		assertThat(ruleToClassMapping).containsKey(refactoringOperationKey);
		assertThat(botIssue.getRefactoringOperation()).isEqualTo(refactoringOperationKey);
		assertThat(botIssue.getLine()).isEqualTo(15);
		assertThat(botIssue.getRefactorString()).isEqualTo("newMethodName");
	}

	@Test
	public void testCommentToIssueMappingRemoveParameter() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_REMOVE_PARAM);

		// assert
		String refactoringOperationKey = "Remove Parameter";
		assertThat(ruleToClassMapping).containsKey(refactoringOperationKey);
		assertThat(botIssue.getRefactoringOperation()).isEqualTo(refactoringOperationKey);
		assertThat(botIssue.getLine()).isEqualTo(20);
		assertThat(botIssue.getRefactorString()).isEqualTo("unusedParam");
	}

	@Test
	public void testCheckComment() {
		GrammarService grammarService = new GrammarService(fileService);
		assertThat(grammarService.checkComment(VALID_COMMENT_ADD_OVERRIDE)).isTrue();
		assertThat(grammarService.checkComment(VALID_COMMENT_ADD_OVERRIDE)).isTrue();
		assertThat(grammarService.checkComment(VALID_COMMENT_REORDER_MODIFIER)).isTrue();
		assertThat(grammarService.checkComment(VALID_COMMENT_RENAME_METHOD)).isTrue();
		assertThat(grammarService.checkComment(VALID_COMMENT_REMOVE_PARAM)).isTrue();

		assertThat(grammarService.checkComment("BOT ADD ANNOTATION")).isFalse();
		assertThat(grammarService.checkComment("BOT RENAME METHOD")).isFalse();
		assertThat(grammarService.checkComment("BOT, BOT, on the wall, who's the fairest of them all?")).isFalse();
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
