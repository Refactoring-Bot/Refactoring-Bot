package de.refactoringbot.api.main;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.exceptions.GitLabAPIException;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ApiGrabberInterface {
    BotPullRequests getRequestsWithComments(GitConfiguration gitConfig)
            throws URISyntaxException, GitHubAPIException, IOException, GitLabAPIException;

    void replyToUserInsideBotRequest(BotPullRequest request, BotPullRequestComment comment,
                                     GitConfiguration gitConfig) throws Exception;

    void replyToUserForFailedRefactoring(BotPullRequest request, BotPullRequestComment comment,
                                         GitConfiguration gitConfig, String errorMessage) throws Exception;

    void checkBranch(GitConfiguration gitConfig, String branchName) throws Exception;

    GitConfiguration createConfigurationForRepo(GitConfigurationDTO configuration) throws Exception;

    void deleteRepository(GitConfiguration gitConfig) throws Exception;

    GitConfiguration createFork(GitConfiguration gitConfig) throws Exception;

    void makeCreateRequestWithAnalysisService(BotIssue issue, GitConfiguration gitConfig, String newBranch)
            throws Exception;
}
