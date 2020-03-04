package de.refactoringbot.services.main;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.CoreConfig.AutoCRLF;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.configuration.FileHoster;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.exceptions.GitWorkflowException;

/**
 * This class uses git programmatically with JGIT.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class GitService {

	@Autowired
	BotConfiguration botConfig;

	private static final Logger logger = LoggerFactory.getLogger(GitService.class);

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
		try (Git git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
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
		try (Git git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			// Fetch data
			if (gitConfig.getRepoService().equals(FileHoster.github)) {
				git.fetch().setRemote("upstream")
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitConfig.getBotToken(), ""))
						.call();
			} else {
				git.fetch().setRemote("upstream").setCredentialsProvider(
						new UsernamePasswordCredentialsProvider(gitConfig.getBotName(), gitConfig.getBotToken()))
						.call();
			}
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
		try (Git git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			// Open git folder
			// Stash changes
			git.stashApply().call();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException("Failed to stash changes!");
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
			if (gitConfig.getRepoService().equals(FileHoster.github)) {
				git = Git.cloneRepository().setURI(gitConfig.getForkGitLink())
						.setDirectory(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitConfig.getBotToken(), ""))
						.call();
			} else {
				git = Git.cloneRepository().setURI(gitConfig.getForkGitLink())
						.setDirectory(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitConfig.getBotName(),
								gitConfig.getBotToken()))
						.call();
			}
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
	public void commitAndPushChanges(GitConfiguration gitConfig, String commitMessage) throws GitWorkflowException {
		try (Git git = Git.open(new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()))) {
			StoredConfig storedRepoConfig = git.getRepository().getConfig();
			// set autocrlf to true to handle line endings of different operating systems
			// correctly. Otherwise the bot will most likely change the line endings of all
			// files to the default of its operating system.
			// Corresponds to 'git config --global core.autocrlf true'
			storedRepoConfig.setEnum(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF,
					AutoCRLF.TRUE);
			// set filemode explicitly to false
			storedRepoConfig.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE,
					false);
			storedRepoConfig.save();

			// We only add those files to the staging area that have actually been changed.
			// 'git add .' in this JGit config would sometimes cause files to be added
			// without content changes (e.g. due to unpredictable whitespace changes).
			List<DiffEntry> diffEntries = git.diff().call();
			for (DiffEntry diffEntry : diffEntries) {
				git.add().addFilepattern(diffEntry.getOldPath()).call();
			}

			// 'git commit -m'
			git.commit().setMessage(commitMessage).setCommitter(gitConfig.getBotName(), gitConfig.getBotEmail()).call();

			// push with bot credentials
			if (gitConfig.getRepoService().equals(FileHoster.github)) {
				git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitConfig.getBotToken(), ""))
						.call();
			} else {
				git.push().setCredentialsProvider(
						new UsernamePasswordCredentialsProvider(gitConfig.getBotName(), gitConfig.getBotToken()))
						.call();
			}
		} catch (TransportException t) {
			logger.error(t.getMessage(), t);
			throw new GitWorkflowException("Wrong bot token!");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitWorkflowException("Could not successfully perform 'git push'!");
		}
	}

	/**
	 * Calculates the absolute line number of the last line in a given diffhunk (the
	 * *new* line number after changes have been applied). This can be used, for
	 * example, to calculate the position of a pull request comment, which is always
	 * found in the last line of a diffhunk.
	 * 
	 * @param diffHunk
	 * @return absolute line number after the changes have been applied, of the last
	 *         line in a given diffhunk
	 */
	public Integer getLineNumberOfLastLineInDiffHunk(String diffHunk) {
		validateDiffHunk(diffHunk);
		String[] diffHunkLines = diffHunk.split("\\R"); // split at new line
		Integer diffHunkStartPosition = getDiffHunkStartPosition(diffHunkLines[0]);
		return calculateLineNumberOfLastDiffHunkLine(diffHunkStartPosition, diffHunkLines);
	}

	/**
	 * Validates the given diffhunk and throws an exception in case of an invalid
	 * argument
	 * 
	 * @param diffHunk
	 */
	private void validateDiffHunk(String diffHunk) {
		if (!diffHunk.contains("@@")) {
			throw new IllegalArgumentException("Diffhunk has to start with @@");
		}
	}

	/**
	 * 
	 * @param firstLineOfDiffHunk
	 * @return position of the given line (after code changes have been applied)
	 */
	private Integer getDiffHunkStartPosition(String firstLineOfDiffHunk) {
		/*
		 * Every diffhunk starts like '@@ -50,7 +50,7 @@' followed by the changed lines
		 * until the line where the comment is placed. The number located after the
		 * first '+' is the line number where the given diffhunk starts.
		 */
		Integer result = null;

		// split the first line at the '+'
		String[] diffSizeStart = firstLineOfDiffHunk.split("\\+");
		if (diffSizeStart.length >= 2) {
			// split the string after the '+' at ','
			String[] diffSizeEnd = diffSizeStart[1].split(",");
			if (diffSizeEnd.length > 0) {
				try {
					result = Integer.valueOf(diffSizeEnd[0]);
				} catch (NumberFormatException n) {
					return 0;
				}
			}
		}

		return result;
	}

	/**
	 * Calculates the absolute new line number of the last line in the given
	 * diffHunkLines
	 * 
	 * @param diffHunkStartPosition
	 * @param diffHunkLines
	 * @return
	 */
	private Integer calculateLineNumberOfLastDiffHunkLine(Integer diffHunkStartPosition, String[] diffHunkLines) {
		/*
		 * To get the line number of the last line we have to count the lines that are
		 * in our file after the changes have been applied, which means that we have to
		 * ignore the lines that were removed. Those lines start with a "-".
		 */
		Integer lineNumber = diffHunkStartPosition;

		if (diffHunkStartPosition != 0) {
			lineNumber--; // don't count the @@ line if it is not the first line in the file
		}

		for (String line : diffHunkLines) {
			if (line.startsWith("+") || line.startsWith(" ")) {
				// line added or unchanged
				lineNumber++;
			}
		}

		return lineNumber;
	}
}
