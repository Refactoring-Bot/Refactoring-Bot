package de.refactoringbot.services.wit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.api.wit.WitDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
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
	 * @param comment
	 * @return botIssue
	 * @throws ReviewCommentUnclearException
	 * @throws WitAPIException
	 */
	public BotIssue createBotIssue(BotPullRequestComment comment)
			throws ReviewCommentUnclearException, WitAPIException {
		BotIssue issue = new BotIssue();

		issue.setCommentServiceID(comment.getCommentID().toString());
		issue.setLine(comment.getPosition());
		issue.setFilePath(comment.getFilepath());

		mapCommentBodyToIssue(issue, comment.getCommentBody());
		return issue;
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
		commentBody = dataAnonymizer.anonymizeComment(commentBody);
		logger.info("Anonymized comment: {}", commentBody);

		WitObject witObject = witDataGrabber.getWitObjectFromComment(commentBody);

		// If wit returns multiple operations or no refactoring at all
		if (witObject.getEntities().getRefactoring().size() != 1) {
			throw new ReviewCommentUnclearException("Not sure what refactoring to execute!");
		}

		// Read unique operation + object
		WitEntity refactoring = witObject.getEntities().getRefactoring().get(0);

		if (refactoring.getValue().equals("ADD-ANNOTATION")) {
			handleCommentForAddAnnotation(issue, witObject);
		} else if (refactoring.getValue().equals("REORDER-MODIFIER")) {
			handleCommentForReorderModifier(issue, commentBody, witObject);
		} else if (refactoring.getValue().equals("RENAME-METHOD")) {
			handleCommentForRenameMethod(issue, commentBody, witObject);
		} else if (refactoring.getValue().equals("REMOVE-PARAMETER")) {
			handleCommentForRemoveParameter(issue, commentBody, witObject);
		} else {
			throw new ReviewCommentUnclearException("I don't know what refactoring you want me to perform!");
		}
	}

	private void handleCommentForAddAnnotation(BotIssue issue, WitObject witObject)
			throws ReviewCommentUnclearException {
		boolean isAnnotationAmbiguous = (witObject.getEntities().getJavaAnnotations().size() != 1); 
		if (isAnnotationAmbiguous) {
			throw new ReviewCommentUnclearException(
					"You proposed a 'add annotation' refactoring without telling me what annotation to add!");
		}

		WitEntity javaAnnot = witObject.getEntities().getJavaAnnotations().get(0);

		if (javaAnnot.getValue().equals("Override")) {
			issue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
		} else {
			throw new ReviewCommentUnclearException("Adding '" + javaAnnot.getValue() + "' is not supported!");
		}
	}

	private void handleCommentForReorderModifier(BotIssue issue, String commentBody, WitObject witObject) {
		// Annotations or refactoring strings are not involved
		if (!witObject.getEntities().getRefactoringString().isEmpty()
				|| !witObject.getEntities().getJavaAnnotations().isEmpty()) {
			logger.warn(
					"Wit detected an 'reorder string' and/or 'annotation' on a 'reorder modifier' refactoring! Comment: {} ",
					commentBody);
		}
		issue.setRefactoringOperation(RefactoringOperations.REORDER_MODIFIER);
	}

	private void handleCommentForRenameMethod(BotIssue issue, String commentBody, WitObject witObject)
			throws ReviewCommentUnclearException {
		boolean isNewMethodNameAmbiguous = (witObject.getEntities().getRefactoringString().size() != 1);
		if (isNewMethodNameAmbiguous) {
			throw new ReviewCommentUnclearException(
					"You proposed a 'rename method' refactoring without telling me the new method name!");
		}
		
		// Annotations are not involved
		if (!witObject.getEntities().getJavaAnnotations().isEmpty()) {
			logger.warn("Wit detected an 'annotation' on a 'rename method' refactoring! Comment: {}", commentBody);
		}
		
		WitEntity refStr = witObject.getEntities().getRefactoringString().get(0);
		issue.setRefactoringOperation(RefactoringOperations.RENAME_METHOD);
		issue.setRefactorString(refStr.getValue());
	}

	private void handleCommentForRemoveParameter(BotIssue issue, String commentBody, WitObject witObject)
			throws ReviewCommentUnclearException {
		boolean isParameterNameAmbiguous = (witObject.getEntities().getRefactoringString().size() != 1);
		if (isParameterNameAmbiguous) {
			throw new ReviewCommentUnclearException(
					"You proposed a 'remove parameter' refactoring without telling me the name of the parameter you want to remove!");
		}
		
		// Annotations are not involved
		if (!witObject.getEntities().getJavaAnnotations().isEmpty()) {
			logger.warn("Wit detected an 'annotation' on a 'remove parameter' refactoring! Comment: {}", commentBody);
		}

		WitEntity refStr = witObject.getEntities().getRefactoringString().get(0);
		issue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
		issue.setRefactorString(refStr.getValue());
	}
}
