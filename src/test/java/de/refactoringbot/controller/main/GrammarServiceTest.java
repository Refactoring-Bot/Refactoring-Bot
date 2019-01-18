package de.refactoringbot.controller.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import de.refactoringbot.services.main.FileService;
import de.refactoringbot.services.main.GrammarService;

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
		assertTrue(ruleToClassMapping.containsKey(refactoringOperationKey));
		assertEquals(refactoringOperationKey, botIssue.getRefactoringOperation());
		assertEquals(Integer.valueOf(5), botIssue.getLine());
	}

	@Test
	public void testCommentToIssueMappingReorderModifiers() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_REORDER_MODIFIER);

		// assert
		String refactoringOperationKey = "Reorder Modifier";
		assertTrue(ruleToClassMapping.containsKey(refactoringOperationKey));
		assertEquals(refactoringOperationKey, botIssue.getRefactoringOperation());
		assertEquals(Integer.valueOf(10), botIssue.getLine());
	}

	@Test
	public void testCommentToIssueMappingRenameMethod() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_RENAME_METHOD);

		// assert
		String refactoringOperationKey = "Rename Method";
		assertTrue(ruleToClassMapping.containsKey(refactoringOperationKey));
		assertEquals(refactoringOperationKey, botIssue.getRefactoringOperation());
		assertEquals(Integer.valueOf(15), botIssue.getLine());
		assertEquals("newMethodName", botIssue.getRefactorString());
	}

	@Test
	public void testCommentToIssueMappingRemoveParameter() throws Exception {
		// arrange + act
		BotIssue botIssue = getIssueAfterMapping(VALID_COMMENT_REMOVE_PARAM);

		// assert
		String refactoringOperationKey = "Remove Parameter";
		assertTrue(ruleToClassMapping.containsKey(refactoringOperationKey));
		assertEquals(refactoringOperationKey, botIssue.getRefactoringOperation());
		assertEquals(Integer.valueOf(20), botIssue.getLine());
		assertEquals("unusedParam", botIssue.getRefactorString());
	}

	@Test
	public void testCheckComment() {
		GrammarService grammarService = new GrammarService(fileService);
		assertTrue(grammarService.checkComment(VALID_COMMENT_ADD_OVERRIDE));
		assertTrue(grammarService.checkComment(VALID_COMMENT_REORDER_MODIFIER));
		assertTrue(grammarService.checkComment(VALID_COMMENT_RENAME_METHOD));
		assertTrue(grammarService.checkComment(VALID_COMMENT_REMOVE_PARAM));

		assertFalse(grammarService.checkComment("BOT ADD ANNOTATION"));
		assertFalse(grammarService.checkComment("BOT RENAME METHOD"));
		assertFalse(grammarService.checkComment("BOT, BOT, on the wall, who's the fairest of them all?"));
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
