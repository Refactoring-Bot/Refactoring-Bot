package de.refactoringbot.api.main;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.naming.OperationNotSupportedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringbot.api.github.GithubDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.exceptions.GitLabAPIException;
import de.refactoringbot.model.github.pullrequest.GithubCreateRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequests;
import de.refactoringbot.model.github.repository.GithubRepository;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.services.github.GithubObjectTranslator;


/**
 * This class transfers all Rest-Requests to correct APIs and returns all
 * objects as translated bot objects.
 *
 * @author Stefan Basaric
 */

@Component

public class githubApiGrabber extends ApiAnalysisGrabber implements ApiGrabberInterface {

    @Autowired
    GithubDataGrabber githubGrabber;

    @Autowired
    GithubObjectTranslator githubTranslator;


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

        GithubPullRequests githubRequests = githubGrabber.getAllPullRequests(gitConfig);
        botRequests = githubTranslator.translateRequests(githubRequests, gitConfig);


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

        githubGrabber.responseToBotComment(githubTranslator.createReplyComment(comment, null), gitConfig,
                request.getRequestNumber());


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

        githubGrabber.responseToBotComment(githubTranslator.createFailureReply(comment, errorMessage), gitConfig,
                request.getRequestNumber());


    }

    /**
     * Check if Branch exists on repository.
     *
     * @param gitConfig
     * @throws Exception
     */
    public void checkBranch(GitConfiguration gitConfig, String branchName) throws Exception {

        githubGrabber.checkBranch(gitConfig, branchName);


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


        GithubRepository githubRepo = githubGrabber.checkRepository(configuration.getRepoName(),
                configuration.getRepoOwner(), configuration.getBotToken(), configuration.getFilehosterApiLink());
        githubGrabber.checkGithubUser(configuration.getBotName(), configuration.getBotToken(),
                configuration.getBotEmail(), configuration.getFilehosterApiLink());

        // Create git configuration and a fork
        gitConfig = githubTranslator.createConfiguration(configuration, githubRepo.getUrl(),
                githubRepo.getHtmlUrl());
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

        githubGrabber.deleteRepository(gitConfig);

    }

    /**
     * This method creates a fork of a repository of a filehoster.
     *
     * @param gitConfig
     * @throws Exception
     */
    public GitConfiguration createFork(GitConfiguration gitConfig) throws Exception {

        GithubRepository githubFork = githubGrabber.createFork(gitConfig);
        gitConfig = githubTranslator.addForkDetailsToConfiguration(gitConfig, githubFork.getUrl(),
                githubFork.getHtmlUrl());
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

        // Create PR object
        GithubCreateRequest createRequest = githubTranslator.makeCreateRequestWithAnalysisService(issue, gitConfig,
                newBranch);
        // Create PR on filehoster
        githubGrabber.createRequest(createRequest, gitConfig);

    }


}
