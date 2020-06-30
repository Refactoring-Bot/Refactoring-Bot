package de.refactoringbot.services.main;

import de.refactoringbot.api.github.GithubDataGrabber;
import de.refactoringbot.api.main.ApiGrabber;
import de.refactoringbot.model.configuration.ConfigurationRepository;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.model.gituser.GitUser;
import de.refactoringbot.model.gituser.GitUserDTO;
import de.refactoringbot.model.gituser.GitUserRepository;
import de.refactoringbot.services.github.GithubObjectTranslator;

import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.Optional;

/**
 * This class contains functions regarding the user.
 *
 * @author Hai Duy Dam
 */
@Service
public class GitUserService {

    private final ModelMapper modelMapper;

    @Autowired
    ConfigurationRepository configRepo;
    @Autowired
    GitUserRepository repo;
    @Autowired
    GithubObjectTranslator translator;
    @Autowired
    GithubDataGrabber githubGrabber;


    private static final Logger logger = LoggerFactory.getLogger(GitUserService.class);

    public GitUserService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * This method checks if a gitUser exists and returns it if possible.
     *
     * @param gitUserId
     * @return savedGitUser
     * @throws Exception
     */
    public GitUser checkGitUserExistence(Long gitUserId)
            throws DatabaseConnectionException, NotFoundException {

        Optional<GitUser> existsGitUser;
        GitUser savedGitUser = null;
        try {
            // Try to get the Git-User with the given ID
            existsGitUser = repo.getGitUserById(gitUserId);

        } catch (Exception e) {
            // Print exception and abort if database error occurs
            logger.error(e.getMessage(), e);
            throw new DatabaseConnectionException("Connection with database failed!");
        }

        // Read GitUser if possible
        if (existsGitUser.isPresent()) {
            savedGitUser = existsGitUser.get();
        } else {
            throw new NotFoundException("GitUser with given ID does not exist in the database!");
        }

        return savedGitUser;
    }

    /**
     * This method checks the user input and adds a GitUser.
     *
     * @param newGitUser
     * @return gitUser
     * @throws Exception
     */
    public GitUser addGitUser(GitUserDTO newGitUser) throws Exception {
        GitUser addedGitUser = new GitUser();
        modelMapper.map(newGitUser, addedGitUser);

        // Check if Github User valid
        githubGrabber.checkGithubUser(newGitUser.getGitUserName(), newGitUser.getGitUserToken(), newGitUser.getGitUserEmail(), newGitUser.getFilehosterApiLink());

        // Try to save the added Gituser
        try {
            addedGitUser = repo.save(addedGitUser);
        } catch (Exception e) {
            // Print exception and abort if database error occurs
            logger.error(e.getMessage(), e);
            throw new DatabaseConnectionException("Connection with database failed!");
        }

        return addedGitUser;
    }

    /**
     * This method checks the user input and updates a GitUser.
     *
     * @param savedGitUser
     * @return updatedGitUser
     * @throws Exception
     */
    public GitUser updateGitUser(GitUser savedGitUser) throws Exception {
        savedGitUser = repo.save(savedGitUser);

        return savedGitUser;
    }

    /**
     * This method returns all gitUsers stored inside the database.
     *
     * @return allGitUsers
     * @throws DatabaseConnectionException
     */
    public Iterable<GitUser> getAllGitUsers() throws DatabaseConnectionException {
        Iterable<GitUser> allGitUsers;
        try {
            allGitUsers = repo.findAll();
            return allGitUsers;
        } catch (Exception e) {
            // Print exception and abort if database error occurs
            logger.error(e.getMessage(), e);
            throw new DatabaseConnectionException("Connection with database failed!");
        }
    }

    /**
     * This method deletes a gitUser from the database.
     *
     * @param gitUser
     * @return userFeedback
     * @throws DatabaseConnectionException
     */
    public ResponseEntity<?> deleteGitUser(GitUser gitUser) {
        // Init feedback
        String userFeedback = null;

        // Delete gitUser from the database
        try {
            repo.delete(gitUser);
            userFeedback = "GitUser deleted from database!";
        } catch (Exception d) {
            logger.error(d.getMessage(), d);
            return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(userFeedback, HttpStatus.OK);
    }
}
