package de.refactoringbot.model.refactoredissue;

import java.util.Optional;

import javax.transaction.Transactional;

import de.refactoringbot.model.configuration.FileHoster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * This interface is used to communicate with the database. It also implements
 * Springs CrudRepository for direct access to existing CRUD-Methods.
 * 
 * @author Stefan Basaric
 *
 */
@Transactional
public interface RefactoredIssueRepository extends CrudRepository<RefactoredIssue, Long> {

	@Query("SELECT a FROM RefactoredIssue a WHERE a.repoService=:repoService")
	Iterable<RefactoredIssue> getAllServiceRefactorings(@Param("repoService") String repoService);

	@Query("SELECT a FROM RefactoredIssue a WHERE a.repoService=:repoService and a.repoOwner=:repoOwner")
	Iterable<RefactoredIssue> getAllUserIssues(@Param("repoService") String repoService,
			@Param("repoOwner") String repoOwner);

	@Query("SELECT a FROM RefactoredIssue a WHERE a.repoService=:repoService and a.commentServiceID=:commentServiceID")
	Optional<RefactoredIssue> refactoredComment(@Param("repoService") FileHoster repoService,
			@Param("commentServiceID") String commentServiceID);

	@Query("SELECT a FROM RefactoredIssue a WHERE a.commentServiceID=:commentServiceID")
	Optional<RefactoredIssue> refactoredAnalysisIssue(@Param("commentServiceID") String commentServiceID);

}
