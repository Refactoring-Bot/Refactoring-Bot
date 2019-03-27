package de.refactoringbot.services.github;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import de.refactoringbot.model.github.pullrequestcomment.ReplyComment;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;

public class GithubObjectTranslatorTest {

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
}
