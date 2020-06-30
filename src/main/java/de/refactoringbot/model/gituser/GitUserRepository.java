package de.refactoringbot.model.gituser;

import java.util.Optional;

import de.refactoringbot.model.configuration.FileHoster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * This interface is used to communicate with the database. It also implements
 * Springs CrudRepository for direct access to existing CRUD-Methods.
 * 
 * @author Hai Duy Dam
 *
 */
@Transactional
public interface GitUserRepository extends CrudRepository<GitUser, Long> {

	@Query("SELECT a FROM GitUser a WHERE a.gitUserName=:gitUserName")
	public Optional<GitUser> getGitUserByName(@Param("gitUserName") String gitUserName);

	@Query("SELECT a FROM GitUser a WHERE a.gitUserId=:gitUserId")
	public Optional<GitUser> getGitUserById(@Param("gitUserId") Long gitUserId);

	@Query("SELECT a FROM GitUser a WHERE a.gitUserEmail=:gitUserEmail")
	public Optional<GitUser> getGitUserByEmail(@Param("gitUserEmail") String gitUserEmail);

	@Query("SELECT a FROM GitUser a WHERE a.repoService=:repoService")
	public Optional<GitUser> getGitUserByRepoService(@Param("repoService") FileHoster repoService);

}
