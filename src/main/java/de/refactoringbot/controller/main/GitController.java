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

/**
 * This class uses git programmicaly with JGIT.
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
	 * @throws Exception
	 */
	public void initLocalWorkspace(GitConfiguration gitConfig) throws Exception {
		// Clone fork
		cloneRepository(gitConfig);
		// Add remote to fork
		addRemote(gitConfig);
	}

	/**
	 * This method adds an Remote to the fork/bot-repository.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 */
	public void addRemote(GitConfiguration gitConfig) throws Exception {
		Git git = null;
		try {
			// Open git folder
			git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()));
			// Add Remote as 'upstream'
			RemoteAddCommand remoteAddCommand = git.remoteAdd();
			remoteAddCommand.setName("upstream");
			remoteAddCommand.setUri(new URIish(gitConfig.getRepoGitLink()));
			remoteAddCommand.call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Could not add as remote " + "'" + gitConfig.getRepoGitLink() + "' successfully!");
		} finally {
			// Close git if possible
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * This method fetches data from the 'upstrem' remote.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 */
	public void fetchRemote(GitConfiguration gitConfig) throws Exception {
		Git git = null;
		try {
			// Open git folder
			git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()));
			// Fetch data
			git.fetch().setRemote("upstream").call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Could not fetch data from 'upstream'!");
		} finally {
			// Close git if possible
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * This method stashes all changes since the last commit.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 */
	public void stashChanges(GitConfiguration gitConfig) throws Exception {
		Git git = null;
		try {
			// Open git folder
			git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()));
			// Stash changes
			git.stashApply().call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Faild to stash changes!");
		} finally {
			// Close git if possible
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * This method clones an repository with its git url.
	 * 
	 * @param repoURL
	 * @throws Exception
	 */
	public void cloneRepository(GitConfiguration gitConfig) throws Exception {
		Git git = null;
		try {
			// Clone repository into git folder
			git = Git.cloneRepository().setURI(gitConfig.getForkGitLink())
					.setDirectory(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))
					.call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Faild to clone " + "'" + gitConfig.getForkGitLink() + "' successfully!");
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
	 * @param id
	 * @throws Exception
	 */
	public void createBranch(GitConfiguration gitConfig, String branchName, String newBranch, String origin)
			throws Exception {
		Git git = null;
		try {
			// Open git folder
			git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()));
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
			throw new Exception("Branch with the name " + "'" + newBranch + "' could not be created!");
		} finally {
			// Close git if possible
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * This method switches the branch.
	 * 
	 * @param branchName
	 * @throws Exception
	 */
	public void switchBranch(GitConfiguration gitConfig, String branchName) throws Exception {
		Git git = null;
		try {
			// Open git folder
			git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()));
			// Switch branch
			@SuppressWarnings("unused")
			Ref ref = git.checkout().setName(branchName).call();
			// If branch does not exist localy anymore
		} catch (RefNotFoundException r) {
			// Recreate branch with current branch data from remote origin
			createBranch(gitConfig, branchName, branchName, "origin");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Could not switch to the branch with the name " + "'" + branchName + "'!");
		} finally {
			// Close git if possible
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * This method performs 'git push' programmically
	 * 
	 * @throws Exception
	 */
	public void pushChanges(GitConfiguration gitConfig, String commitMessage) throws Exception {
		Git git = null;
		try {
			// Open git folder
			git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()));
			// Perform 'git add .'
			git.add().addFilepattern(".").call();
			// Perform 'git commit -m'
			git.commit().setMessage(commitMessage).setCommitter(gitConfig.getBotName(), gitConfig.getBotEmail()).call();
			// Push with bot credenials
			git.push()
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(gitConfig.getBotName(), gitConfig.getBotPassword()))
					.call();
		} catch (TransportException t) {
			logger.error(t.getMessage(), t);
			throw new Exception("Wrong bot password!");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Could not successfully perform 'git push'!");
		} finally {
			// Close git if possible
			if (git != null) {
				git.close();
			}
		}
	}
}
