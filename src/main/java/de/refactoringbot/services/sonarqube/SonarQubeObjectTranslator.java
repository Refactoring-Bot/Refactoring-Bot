package de.refactoringbot.services.sonarqube;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.sonarqube.SonarIssue;
import de.refactoringbot.model.sonarqube.SonarQubeIssues;
import de.refactoringbot.refactoring.RefactoringOperations;

/**
 * This class translates SonarQube Objects into Bot-Objects.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class SonarQubeObjectTranslator {

	/**
	 * This method translates all SonarQubeIssues to BotIssues.
	 * 
	 * @param issues
	 * @return botIssue
	 * @throws IOException
	 */
	public List<BotIssue> translateSonarIssue(SonarQubeIssues issues, GitConfiguration gitConfig) throws IOException {
		List<BotIssue> botIssues = new ArrayList<>();

		for (SonarIssue issue : issues.getIssues()) {
			BotIssue botIssue = new BotIssue();

			// Create filepath
			String project = issue.getProject();
			String component = issue.getComponent();

			botIssue.setFilePath(Paths.get(component.substring(project.length() + 1, component.length())).toString());
			botIssue.setLine(issue.getLine());
			botIssue.setCommentServiceID(issue.getKey());

			// Set creation date to determine the age of the issue
			botIssue.setCreationDate(issue.getCreationDate());

			// Translate SonarQube rule
			switch (issue.getRule()) {
			case "squid:S1161":
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssues.add(botIssue);
				break;
			case "squid:ModifiersOrderCheck":
				botIssue.setRefactoringOperation(RefactoringOperations.REORDER_MODIFIER);
				botIssues.add(botIssue);
				break;
			case "squid:CommentedOutCodeLine":
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssues.add(botIssue);
				break;
			case "squid:S1172":
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
				botIssue.setRefactorString(getParameterName(issue));
				botIssues.add(botIssue);
				break;
			default:
				botIssue.setRefactoringOperation(RefactoringOperations.UNKNOWN);
				break;
			}
		}

		return botIssues;
	}

	/**
	 * This method scans the message of a "RemoveParameter" issue of SonarQube and
	 * returns the parameter name of the unused parameter.
	 * 
	 * @param issue
	 * @return parameterName
	 */
	public String getParameterName(SonarIssue issue) {
		String message = issue.getMessage();
		String[] splitMessage = message.split(" ");
		String paramPartOfMessage = "";
		for (int i = 0; i < splitMessage.length; i++) {
			if (splitMessage[i].equals("parameter") && i < splitMessage.length - 1) {
				paramPartOfMessage = splitMessage[i + 1];
			}
		}
		return paramPartOfMessage.substring(1, paramPartOfMessage.length() - 2);
	}

	/**
	 * This method is used to build a absolute path of the file that contains the
	 * Sonar-Issue.
	 * 
	 * @param gitConfig
	 * @param sonarIssuePath
	 * @return absolutePath
	 * @throws IOException
	 */
	public String buildIssuePath(GitConfiguration gitConfig, String sonarIssuePath) throws IOException {
		// Create full path for sonar issue
		File issuePath = new File(gitConfig.getRepoFolder() + File.separator + sonarIssuePath);

		// If the analysis was made from the root folder we're done
		if (issuePath.exists()) {
			sonarIssuePath = issuePath.toString();
		} else {
			// If not we go through subdirectories to check if they match the issue paths
			File[] directories = new File(gitConfig.getRepoFolder()).listFiles(File::isDirectory);

			for (File file : directories) {
				issuePath = new File(file.getAbsolutePath() + File.separator + sonarIssuePath);
				if (issuePath.exists()) {
					sonarIssuePath = issuePath.toString();
					break;
				}
			}
		}

		if (!issuePath.exists()) {
			throw new IOException("Unable to locate issue path.");
		}

		// Cut path outside the repository
		return StringUtils.difference(gitConfig.getRepoFolder(), sonarIssuePath);
	}
}
