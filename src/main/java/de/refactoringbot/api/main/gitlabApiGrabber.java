package de.refactoringbot.api.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringbot.api.github.GithubDataGrabber;
import de.refactoringbot.api.gitlab.GitlabDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.AnalysisProvider;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.exceptions.GitLabAPIException;
import de.refactoringbot.model.exceptions.SonarQubeAPIException;
import de.refactoringbot.model.github.pullrequest.GithubCreateRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequests;
import de.refactoringbot.model.github.repository.GithubRepository;
import de.refactoringbot.model.gitlab.pullrequest.GitLabCreateRequest;
import de.refactoringbot.model.gitlab.pullrequest.GitLabPullRequests;
import de.refactoringbot.model.gitlab.repository.GitLabRepository;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.services.github.GithubObjectTranslator;
import de.refactoringbot.services.gitlab.GitlabObjectTranslator;
import de.refactoringbot.services.main.BotService;


/**
 * This class transfers all Rest-Requests to correct APIs and returns all
 * objects as translated bot objects.
 *
 * @author Stefan Basaric
 */

@Component

public class gitlabApiGrabber extends ApiAnalysisGrabber implements ApiGrabberInterface {

    @Autowired
    GithubDataGrabber githubGrabber;
    @Autowired
    GitlabDataGrabber gitlabGrabber;
    @Autowired
    GithubObjectTranslator githubTranslator;
    @Autowired
    GitlabObjectTranslator gitlabTranslator;


    /**
     * This method gets all requests with all comments from an api translated into a
     * bot object.
     *
     * @param gitConfig
     * @return botRequests
     * @throws URISyntaxException
     * @throws GitHubAPIException
     * @throws IOException
     * @throws GitLabAPIException
     */
    public BotPullRequests getRequestsWithComments(GitConfiguration gitConfig)
            throws URISyntaxException, GitHubAPIException, IOException, GitLabAPIException {

        BotPullRequests botRequests = null;

        GitLabPullRequests gitlabRequests = gitlabGrabber.getAllPullRequests(gitConfig);
        botRequests = gitlabTranslator.translateRequests(gitlabRequests, gitConfig);

        return botRequests;
    }

    /**
     * This method replies to User inside a Pull-Request that belongs to a Bot if
     * the refactoring was successful.
     *
     * @param request
     * @param gitConfig
     * @throws Exception
     * @throws OperationNotSupportedException
     */
    public void replyToUserInsideBotRequest(BotPullRequest request, BotPullRequestComment comment,
                                            GitConfiguration gitConfig) throws Exception {

        gitlabGrabber.respondToUser(gitConfig, request.getRequestNumber(), comment.getDiscussionID(),
                comment.getCommentID(), gitlabTranslator.getReplyComment(null));

    }

    /**
     * Reply to comment if refactoring failed.
     *
     * @param request
     * @param gitConfig
     * @throws Exception
     */
    public void replyToUserForFailedRefactoring(BotPullRequest request, BotPullRequestComment comment,
                                                GitConfiguration gitConfig, String errorMessage) throws Exception {

        gitlabGrabber.respondToUser(gitConfig, request.getRequestNumber(), comment.getDiscussionID(),
                comment.getCommentID(), errorMessage);

    }

    /**
     * Check if Branch exists on repository.
     *
     * @param gitConfig
     * @throws Exception
     */
    public void checkBranch(GitConfiguration gitConfig, String branchName) throws Exception {

        gitlabGrabber.checkBranch(gitConfig, branchName);

    }

    /**
     * This method checks the user input and creates a git configuration.
     *
     * @param configuration
     * @return gitConfig
     * @throws Exception
     */
    public GitConfiguration createConfigurationForRepo(GitConfigurationDTO configuration) throws Exception {
        GitConfiguration gitConfig = null;
        // Check analysis service data
        checkAnalysisService(configuration);


        GitLabRepository gitlabRepo = gitlabGrabber.checkRepository(configuration.getRepoName(),
                configuration.getRepoOwner(), configuration.getBotToken(), configuration.getFilehosterApiLink());
        gitlabGrabber.checkGitlabUser(configuration.getBotName(), configuration.getBotToken(),
                configuration.getBotEmail(), configuration.getFilehosterApiLink());

        // Create git configuration and a fork
        gitConfig = gitlabTranslator.createConfiguration(configuration, gitlabRepo.getLinks().getSelf(),
                gitlabRepo.getHttpUrlToRepo());
        return gitConfig;

    }

    /**
     * This method deletes a repository of a filehoster.
     *
     * @param gitConfig
     * @throws Exception
     * @throws OperationNotSupportedException
     */
    public void deleteRepository(GitConfiguration gitConfig) throws Exception {

        gitlabGrabber.deleteRepository(gitConfig);

    }

    /**
     * This method creates a fork of a repository of a filehoster.
     *
     * @param gitConfig
     * @throws Exception
     */
    public GitConfiguration createFork(GitConfiguration gitConfig) throws Exception {

        GitLabRepository gitlabFork = gitlabGrabber.createFork(gitConfig);
        gitConfig = gitlabTranslator.addForkDetailsToConfiguration(gitConfig, gitlabFork.getLinks().getSelf(),
                gitlabFork.getHttpUrlToRepo());
        return gitConfig;

    }


    /**
     * This method creates a request on a filehoster if the refactoring was
     * performed with issues from a analysis tool.
     *
     * @param issue
     * @param gitConfig
     * @param newBranch
     * @throws Exception
     */
    public void makeCreateRequestWithAnalysisService(BotIssue issue, GitConfiguration gitConfig, String newBranch)
            throws Exception {
        // Create PR Object
        GitLabCreateRequest gitlabCreateRequest = gitlabTranslator.makeCreateRequestWithAnalysisService(issue,
                gitConfig, newBranch);
        // Create PR on filehoster
        gitlabGrabber.createRequest(gitlabCreateRequest, gitConfig);

    }


}
