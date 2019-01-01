package de.refactoringBot.controller.sonarQube;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.sonarQube.SonarIssue;
import de.refactoringBot.model.sonarQube.SonarQubeIssues;
import de.refactoringBot.refactoring.RefactoringOperations;

/**
 * This class translates SonarCube Objects into Bot-Objects.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class SonarQubeObjectTranslator {

	@Autowired
	RefactoringOperations operations;

	/**
	 * This method translates all SonarCubeIssues to BotIssues.
	 * 
	 * @param issue
	 * @return botIssue
	 */
	public List<BotIssue> translateSonarIssue(SonarQubeIssues issues, GitConfiguration gitConfig) {
		// Create empty list of bot issues
		List<BotIssue> botIssues = new ArrayList<>();

		// Iterate all SonarCube issues
		for (SonarIssue issue : issues.getIssues()) {
			// Create bot issue
			BotIssue botIssue = new BotIssue();

			// Create filepath
			String project = issue.getProject();
			String component = issue.getComponent();
			String sonarIssuePath = Paths.get(component.substring(project.length() + 1, component.length())).toString();

			// Create full path for sonar issue
			sonarIssuePath = gitConfig.getSrcFolder().substring(0, gitConfig.getSrcFolder().length() - 3)
					+ sonarIssuePath;

			// Cut path outside the repository
			String translatedPath = StringUtils.difference(gitConfig.getRepoFolder(), sonarIssuePath);
			// Remove leading '/'
			translatedPath = translatedPath.substring(1);

			botIssue.setFilePath(translatedPath);

			// Fill object
			botIssue.setLine(issue.getLine());
			botIssue.setCommentServiceID(issue.getKey());

			// Set creation date to determine the age of the issue
			botIssue.setCreationDate(issue.getCreationDate());

			// Translate SonarCube rule
			switch (issue.getRule()) {
			case "squid:S1161":
				botIssue.setRefactoringOperation(operations.ADD_OVERRIDE_ANNOTATION);
				// Add bot issue to list
				botIssues.add(botIssue);
				break;
			case "squid:ModifiersOrderCheck":
				botIssue.setRefactoringOperation(operations.REORDER_MODIFIER);
				// Add bot issue to list
				botIssues.add(botIssue);
				break;
			case "squid:CommentedOutCodeLine":
				botIssue.setRefactoringOperation(operations.REMOVE_COMMENTED_OUT_CODE);
				botIssues.add(botIssue);
				break;
			case "squid:S1172":
				botIssue.setRefactoringOperation(operations.REMOVE_PARAMETER);
				botIssue.setRefactorString(getParameterName(issue));
				botIssues.add(botIssue);
				break;
			default:
				botIssue.setRefactoringOperation(operations.UNKNOWN);
				break;
			}
		}

		return botIssues;
	}

	/**
	 * This method scans the message of a "RemoveParameter" issue of
	 * SonarCloud/SonarQube and returns the parameter name of the unused parameter.
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
		String parameterName = paramPartOfMessage.substring(1, paramPartOfMessage.length() - 2);
		return parameterName;
	}
}
