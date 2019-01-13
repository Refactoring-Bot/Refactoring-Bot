package de.refactoringbot.controller.main;

import java.io.File;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.exceptions.GitWorkflowException;

/**
 * This class uses git programmatically with JGIT.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class GitController {

	@Autowired
	BotConfiguration botConfig;

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(GitController.class);

	/**
	 * This method initialises the workspace.
	 * 
	 * @param gitConfig
	 * @throws GitWorkflowException
	 */
	public void initLocalWorkspace(GitConfiguration gitConfig) throws GitWorkflowException {
		// Clone fork
		cloneRepository(gitConfig);
		// Add remote to fork
		addRemote(gitConfig);
	}

	/**
	 * This method adds an Remote to the fork/bot-repository.
	 * 
	 * @param gitConfig
	 * @throws GitWorkflowException
	 */
	public void addRemote(GitConfiguration gitConfig) throws GitWorkflowException {
		try (Git git = Git.open(
				new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			// Add Remote as 'upstream'
			RemoteAddCommand remoteAddCommand = git.remoteAdd();
			remoteAddCommand.setName("upstream");
			remoteAddCommand.setUri(new URIish(gitConfig.getRepoGitLink()));
			remoteAddCommand.call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException(
					"Could not add as remote " + "'" + gitConfig.getRepoGitLink() + "' successfully!");
		}
	}

	/**
	 * This method fetches data from the 'upstrem' remote.
	 * 
	 * @param gitConfig
	 * @throws GitWorkflowException
	 */
	public void fetchRemote(GitConfiguration gitConfig) throws GitWorkflowException {
		try (Git git = Git.open(
				new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			// Fetch data
			git.fetch().setRemote("upstream").call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException("Could not fetch data from 'upstream'!");
		}
	}

	/**
	 * This method stashes all changes since the last commit.
	 * 
	 * @param gitConfig
	 * @throws GitWorkflowException
	 */
	public void stashChanges(GitConfiguration gitConfig) throws GitWorkflowException {
		try (Git git = Git.open(
				new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			// Open git folder
			// Stash changes
			git.stashApply().call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException("Faild to stash changes!");
		}
	}

	/**
	 * This method clones an repository with its git url.
	 * 
	 * @param gitConfig
	 * @throws GitWorkflowException
	 */
	public void cloneRepository(GitConfiguration gitConfig) throws GitWorkflowException {
		Git git = null;
		try {
			// Clone repository into git folder
			git = Git.cloneRepository().setURI(gitConfig.getForkGitLink())
					.setDirectory(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))
					.call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException("Faild to clone " + "'" + gitConfig.getForkGitLink() + "' successfully!");
		} finally {
			// Close git if possible
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * This method creates a new branch.
	 * 
	 * @param gitConfig
	 * @param branchName
	 * @param origin
	 * @throws BotRefactoringException
	 * @throws GitWorkflowException
	 */
	public void createBranch(GitConfiguration gitConfig, String branchName, String newBranch, String origin)
			throws BotRefactoringException, GitWorkflowException {
		try (Git git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			// Try to create new branch
			@SuppressWarnings("unused")
			Ref ref = git.checkout().setCreateBranch(true).setName(newBranch)
					.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
					.setStartPoint(origin + "/" + branchName).call();
			// Pull data
			git.pull();
			// If branch already exists
		} catch (RefAlreadyExistsException r) {
			logger.error(r.getMessage(), r);
			throw new BotRefactoringException(
					"Issue was already refactored in the past! The bot database might have been resetted but not the fork itself.");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException("Branch with the name " + "'" + newBranch + "' could not be created!");
		}
	}

	/**
	 * This method switches the branch.
	 * 
	 * @param branchName
	 * @throws GitWorkflowException
	 * @throws BotRefactoringException
	 */
	public void switchBranch(GitConfiguration gitConfig, String branchName)
			throws GitWorkflowException, BotRefactoringException {
		try (Git git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			// Switch branch
			@SuppressWarnings("unused")
			Ref ref = git.checkout().setName(branchName).call();
			// If branch does not exist locally anymore
		} catch (RefNotFoundException r) {
			// Recreate branch with current branch data from remote origin
			createBranch(gitConfig, branchName, branchName, "origin");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException("Could not switch to the branch with the name " + "'" + branchName + "'!");
		}
	}

	/**
	 * This method performs 'git push' programmically
	 * 
	 * @throws GitWorkflowException
	 */
	public void pushChanges(GitConfiguration gitConfig, String commitMessage) throws GitWorkflowException {
		try (Git git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			// Perform 'git add .'
			git.add().addFilepattern(".").call();
			// Perform 'git commit -m'
			git.commit().setMessage(commitMessage).setCommitter(gitConfig.getBotName(), gitConfig.getBotEmail()).call();
			// Push with bot credenials
			git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitConfig.getBotToken(), ""))
					.call();
		} catch (TransportException t) {
			logger.error(t.getMessage(), t);
			throw new GitWorkflowException("Wrong bot token!");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException("Could not successfully perform 'git push'!");
		}
	}
}
