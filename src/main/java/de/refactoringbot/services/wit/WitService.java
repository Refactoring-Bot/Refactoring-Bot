package de.refactoringbot.services.wit;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.api.wit.WitDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.ReviewCommentUnclearException;
import de.refactoringbot.model.exceptions.WitAPIException;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.wit.WitEntity;
import de.refactoringbot.model.wit.WitObject;
import de.refactoringbot.refactoring.RefactoringOperations;

/**
 * This class implements functions around the wit.ai service.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class WitService {

	private WitDataGrabber witDataGrabber;
	private DataAnonymizerService dataAnonymizer;

	private static final Logger logger = LoggerFactory.getLogger(WitService.class);

	@Autowired
	public WitService(WitDataGrabber witDataGrabber, DataAnonymizerService dataAnonymizer) {
		this.witDataGrabber = witDataGrabber;
		this.dataAnonymizer = dataAnonymizer;
	}

	/**
	 * This method creates a BotIssue from a message with the help of the wit.ai
	 * service.
	 * 
	 * @param gitConfig
	 * @param comment
	 * @return botIssue
	 * @throws ReviewCommentUnclearException
	 */
	public BotIssue createBotIssue(GitConfiguration gitConfig, BotPullRequestComment comment)
			throws ReviewCommentUnclearException, IOException {
		try {
			BotIssue issue = new BotIssue();

			issue.setCommentServiceID(comment.getCommentID().toString());
			issue.setLine(comment.getPosition());
			issue.setFilePath(comment.getFilepath());

			mapCommentBodyToIssue(issue, comment.getCommentBody());
			return issue;
		} catch (WitAPIException | ReviewCommentUnclearException e) {
			logger.error(e.getMessage(), e);
			throw new ReviewCommentUnclearException(e.getMessage());
		}
	}

	/**
	 * This method maps the information of a comment to the issue.
	 * 
	 * @param issue
	 * @param commentBody
	 * @throws WitAPIException
	 * @throws ReviewCommentUnclearException
	 */
	private void mapCommentBodyToIssue(BotIssue issue, String commentBody)
			throws WitAPIException, ReviewCommentUnclearException {
		// Anonymize possible sensitive data
		commentBody = dataAnonymizer.anonymizeComment(commentBody);

		logger.info("Anonymized comment: " + commentBody);
		// Get Wit-Object from Wit-API
		WitObject witObject = witDataGrabber.getWitObjectFromComment(commentBody);

		// If wit returns multiple operations or none refactorings
		if (witObject.getEntities().getRefactoring().size() != 1) {
			throw new ReviewCommentUnclearException("Not sure what refactoring to execute!");
		}

		// Read unique operation + object
		WitEntity refactoring = witObject.getEntities().getRefactoring().get(0);

		if (refactoring.getValue().equals("ADD-ANNOTATION")) {
			// If 0/2+ possilbe annotations returned
			if (witObject.getEntities().getJavaAnnotations().size() != 1) {
				throw new ReviewCommentUnclearException(
						"You proposed a 'add annotation' refactoring without telling me what annotation to add!");
			}
			// Read unique java annotation
			WitEntity javaAnnot = witObject.getEntities().getJavaAnnotations().get(0);
			// If override annotation returned
			if (javaAnnot.getValue().equals("Override")) {
				issue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				// If some unknown annotation returned
			} else {
				throw new ReviewCommentUnclearException("Adding '" + javaAnnot.getValue() + "' is not supported!");
			}
		}

		else if (refactoring.getValue().equals("REORDER-MODIFIER")) {
			// Annotations or refactoring strings are not involved
			if (witObject.getEntities().getRefactoringString().size() != 0
					|| witObject.getEntities().getJavaAnnotations().size() != 0) {
				logger.warn(
						"Wit detected an 'reorder string' and/or 'annotation' on a 'reorder modifier' refactoring! Comment: "
								+ commentBody);
			}
			issue.setRefactoringOperation(RefactoringOperations.REORDER_MODIFIER);
		}

		else if (refactoring.getValue().equals("RENAME-METHOD")) {
			// If new method name is uncertain
			if (witObject.getEntities().getRefactoringString().size() != 1) {
				throw new ReviewCommentUnclearException(
						"You proposed a 'rename method' refactoring without telling me the new method name!");
			}
			// Annotations are not involved
			if (witObject.getEntities().getJavaAnnotations().size() != 0) {
				logger.warn("Wit detected an 'annotation' on a 'rename method' refactoring! Comment: " + commentBody);
			}
			// Read unique refactoring string (method name object)
			WitEntity refStr = witObject.getEntities().getRefactoringString().get(0);
			issue.setRefactoringOperation(RefactoringOperations.RENAME_METHOD);
			issue.setRefactorString(refStr.getValue());
		}

		else if (refactoring.getValue().equals("REMOVE-PARAMETER")) {
			// If parameter name is uncertain
			if (witObject.getEntities().getRefactoringString().size() != 1) {
				throw new ReviewCommentUnclearException(
						"You proposed a 'remove parameter' refactoring without telling me the name of the parameter you want to remove!");
			}
			// Annotations are not involved
			if (witObject.getEntities().getJavaAnnotations().size() != 0) {
				logger.warn(
						"Wit detected an 'annotation' on a 'remove parameter' refactoring! Comment: " + commentBody);
			}
			// Read unique refactoring string (parameter name object)
			WitEntity refStr = witObject.getEntities().getRefactoringString().get(0);
			issue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
			issue.setRefactorString(refStr.getValue());
		} else {
			throw new ReviewCommentUnclearException("I don't know what refactoring you want me to perform!");
		}
	}
}
