package de.refactoringbot.services.wit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.Assert.assertNotNull;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.refactoringbot.api.wit.WitDataGrabber;
import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.exceptions.ReviewCommentUnclearException;
import de.refactoringbot.model.exceptions.WitAPIException;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.refactoring.RefactoringOperations;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BotConfiguration.class, WitDataGrabber.class, DataAnonymizerService.class,
		WitService.class })
@EnableConfigurationProperties
public class WitServiceIT {

	public final static String WIT_CLIENT_TOKEN_ENV_NAME = "WIT_CLIENT_TOKEN";

	private WitService witService;
	@Mock
	private WitDataGrabber witDataGrabber;
	@Mock
	private BotConfiguration botConfig;
	@Autowired
	DataAnonymizerService dataAnonymizer;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void beforeClass() {
		/*
		 * This test gets skipped if it is not executed on the build server. To
		 * successfully run it on a local machine, this method must not be executed and
		 * a client token for wit API access must be provided in the mock.
		 */
		assumeThat(System.getenv("GITHUB_ACTIONS")).isNotNull();
		assumeThat(System.getenv(WIT_CLIENT_TOKEN_ENV_NAME)).isNotNull();
	}

	@Before
	public void init() {
		witDataGrabber = new WitDataGrabber(botConfig);
		witService = new WitService(witDataGrabber, dataAnonymizer);
		String tokenValue = System.getenv(WIT_CLIENT_TOKEN_ENV_NAME);
		Mockito.when(botConfig.getWitClientToken()).thenReturn(tokenValue);
	}

	@Test
	public void testAddOverrideAnnotationComment() throws Exception {
		// arrange + act
		BotIssue issue = createBotIssueForCommentBody("Hey @Bot, add an override annotation here, ok?");

		// assert
		assertNotNull(issue);
		assertThat(issue.getRefactoringOperation()).isEqualTo(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
	}

	@Test
	public void testAddOverrideAnnotationComment2() throws Exception {
		// arrange + act
		BotIssue issue = createBotIssueForCommentBody("@Bot please add annotation \"Override\" here");

		// assert
		assertNotNull(issue);
		assertThat(issue.getRefactoringOperation()).isEqualTo(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
	}

	@Test
	public void testAddOverrideAnnotationCommentUnclearAnnotation() throws Exception {
		exception.expect(ReviewCommentUnclearException.class);
		createBotIssueForCommentBody("Hey @Bot, add the missing annotation.");
	}

	@Test
	public void testAddOverrideAnnotationCommentUnsupportedAnnotation() throws Exception {
		exception.expect(ReviewCommentUnclearException.class);
		createBotIssueForCommentBody("Hey @Bot, add annotation NotExistingOrUnsupportedOperation.");
	}

	@Test
	public void testReorderModifierComment() throws Exception {
		// arrange + act
		BotIssue issue = createBotIssueForCommentBody("@Bot please reorder these modifiers");

		// assert
		assertNotNull(issue);
		assertThat(issue.getRefactoringOperation()).isEqualTo(RefactoringOperations.REORDER_MODIFIER);
	}

	@Test
	public void testReorderModifierComment2() throws Exception {
		// arrange + act
		BotIssue issue = createBotIssueForCommentBody(
				"@Bot please put the modifiers in an order that complies with the JLS.!");

		// assert
		assertNotNull(issue);
		assertThat(issue.getRefactoringOperation()).isEqualTo(RefactoringOperations.REORDER_MODIFIER);
	}

	@Test
	public void testRenameMethodComment() throws Exception {
		// arrange + act
		BotIssue issue = createBotIssueForCommentBody("@Bot Please rename this method to getABC");

		// assert
		assertNotNull(issue);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(issue.getRefactoringOperation()).isEqualTo(RefactoringOperations.RENAME_METHOD);
		softAssertions.assertThat(issue.getRefactorString()).isEqualTo("getABC");
		softAssertions.assertAll();
	}

	@Test
	public void testRenameMethodComment2() throws Exception {
		// arrange + act
		BotIssue issue = createBotIssueForCommentBody("Could you rename this to \"doSomething\" @Bot?");

		// assert
		assertNotNull(issue);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(issue.getRefactoringOperation()).isEqualTo(RefactoringOperations.RENAME_METHOD);
		softAssertions.assertThat(issue.getRefactorString()).isEqualTo("doSomething");
		softAssertions.assertAll();
	}

	@Test
	public void testRenameMethodCommentUnclearNewName() throws Exception {
		exception.expect(ReviewCommentUnclearException.class);
		createBotIssueForCommentBody("@Bot please rename this method!");
	}

	@Test
	public void testRenameMethodCommentUnclearNewName2() throws Exception {
		exception.expect(ReviewCommentUnclearException.class);
		createBotIssueForCommentBody("@Bot Rename this method, ok?");
	}

	@Test
	public void testRenameMethodCommentUnclearNewName3() throws Exception {
		exception.expect(ReviewCommentUnclearException.class);
		createBotIssueForCommentBody("@Bot Could you rename this method please?");
	}

	@Test
	public void testRemoveParameterComment() throws Exception {
		// arrange + act
		BotIssue issue = createBotIssueForCommentBody("@Bot could you remove parameter unusedParam?");

		// assert
		assertNotNull(issue);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(issue.getRefactoringOperation()).isEqualTo(RefactoringOperations.REMOVE_PARAMETER);
		softAssertions.assertThat(issue.getRefactorString()).isEqualTo("unusedParam");
		softAssertions.assertAll();
	}

	@Test
	public void testRemoveParameterComment2() throws Exception {
		// arrange + act
		BotIssue issue = createBotIssueForCommentBody("@Bot, please remove this parameter: param");

		// assert
		assertNotNull(issue);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(issue.getRefactoringOperation()).isEqualTo(RefactoringOperations.REMOVE_PARAMETER);
		softAssertions.assertThat(issue.getRefactorString()).isEqualTo("param");
		softAssertions.assertAll();
	}

	@Test
	public void testRemoveParamterUnclearParam() throws Exception {
		exception.expect(ReviewCommentUnclearException.class);
		createBotIssueForCommentBody("@Bot please remove the other unused parameter");
	}

	private BotIssue createBotIssueForCommentBody(String commentBody)
			throws ReviewCommentUnclearException, WitAPIException {
		// arrange
		BotPullRequestComment comment = new BotPullRequestComment();
		comment.setCommentID(1);
		comment.setCommentBody(commentBody);

		// act
		return witService.createBotIssue(comment);
	}

}
