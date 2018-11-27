package de.BA.refactoringBot.model.configuration;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * This interface is used to communicate with the database. It also implements
 * Springs CrudRepository for direct access to existing CRUD-Methods.
 * 
 * @author Stefan Basaric
 *
 */
@Transactional
public interface ConfigurationRepository extends CrudRepository<GitConfiguration, Long> {

	@Query("SELECT a FROM GitConfiguration a WHERE a.repoName=:repoName and a.repoOwner=:repoOwner")
	public Optional<GitConfiguration> getConfigByName(@Param("repoName") String repoName,
			@Param("repoOwner") String repoOwner);
	
	@Query("SELECT a FROM GitConfiguration a WHERE a.repoName=:repoName and a.botName=:botName")
	public Optional<GitConfiguration> getConfigByFork(@Param("repoName") String repoName,
			@Param("botName") String botName);

	@Query("SELECT a FROM GitConfiguration a WHERE a.configurationId=:configurationId")
	public Optional<GitConfiguration> getByID(@Param("configurationId") Long configurationId);

}
