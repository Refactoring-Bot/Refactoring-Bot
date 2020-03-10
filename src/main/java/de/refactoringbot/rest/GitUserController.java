package de.refactoringbot.rest;

import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.github.repository.GithubRepository;
import de.refactoringbot.model.gituser.GitUser;
import de.refactoringbot.model.gituser.GitUserDTO;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.services.main.GitUserService;
import de.refactoringbot.api.github.GithubDataGrabber;
import io.swagger.annotations.ApiOperation;
import javassist.NotFoundException;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This method offers an CRUD-Interface as a REST-Interface for the
 * Git-Users.
 *
 * @author Hai Duy Dam
 *
 */
@RestController
@RequestMapping(path = "/git-users")
public class GitUserController {

    @Autowired
    GitUserService gitUserService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    GithubDataGrabber grabber;

    private static final Logger logger = LoggerFactory.getLogger(GitUserController.class);

    /**
     * This method creates an git user with the user inputs.
     *
     * @param newGitUser
     * @return
     */

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Add Git-User")
    public ResponseEntity<Object> add(@RequestBody GitUserDTO newGitUser) {
        GitUser addedGitUser = null;
        try {
            addedGitUser = gitUserService.addGitUser(newGitUser);
            return new ResponseEntity<>(addedGitUser, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * This method updates an git user with the given userId.
     *
     * @param savedGitUser
     * @return updatedGitUser
     */
    @PutMapping(path = "/{gitUserId}", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Update Git-User with gitUser Id")
    public ResponseEntity<?> update(@RequestBody GitUserDTO savedGitUser,
                                    @PathVariable(name = "gitUserId") Long gitUserId) {
        GitUser updatedGitUser = null;
        try {
            updatedGitUser = gitUserService.checkGitUserExistence(gitUserId);
            grabber.checkGithubUser(savedGitUser.getGitUserName(), savedGitUser.getGitUserToken(), savedGitUser.getGitUserEmail(), savedGitUser.getFilehosterApiLink());

        } catch (DatabaseConnectionException d) {
            // Print exception and abort if database error occurs
            logger.error(d.getMessage(), d);
            return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception n) {
            return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
        }

        modelMapper.map(savedGitUser, updatedGitUser);

        // Init database user

        try {
            updatedGitUser = gitUserService.updateGitUser(updatedGitUser);
            return new ResponseEntity<>(updatedGitUser, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method removes a gituser with a specific ID from the database.
     *
     * @param gitUserId
     * @return {feedbackString}
     */

    @DeleteMapping(path = "/{gitUserId}", produces = "application/json")
    @ApiOperation(value = "Delete Git-User with gitUser id")
    public ResponseEntity<?> deleteGitUser(@PathVariable("gitUserId") Long gitUserId) {
        // Check if gitUser exists
        GitUser gitUser = null;
        try {
            gitUser = gitUserService.checkGitUserExistence(gitUserId);
        } catch (DatabaseConnectionException d) {
            // Print exception and abort if database error occurs
            logger.error(d.getMessage(), d);
            return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException n) {
            return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
        }

        // Remove User and respond
        return gitUserService.deleteGitUser(gitUser);
    }

    /**
     * This method returns all gitUser from the database
     *
     * @return allGitUsers
     */
    @GetMapping(produces = "application/json")
    @ApiOperation(value = "Get all Git-User")
    public ResponseEntity<?> getAllGitUsers() {
        try {
            Iterable<GitUser> allGitUsers = gitUserService.getAllGitUsers();
            return new ResponseEntity<>(allGitUsers, HttpStatus.OK);
        } catch (DatabaseConnectionException e) {
            // Print exception and abort if database error occurs
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method returns a gitUser with a specific ID.
     *
     * @param gitUserId
     * @return gitUser
     */
    @GetMapping(path = "/{gitUserId}", produces = "application/json")
    @ApiOperation(value = "Get Git-User with gitUser id")
    public ResponseEntity<?> getGitUserById(@PathVariable("gitUserId")  Long gitUserId) {
        // Check if gitUser exists
        GitUser gitUser = null;
        try {
            gitUser = gitUserService.checkGitUserExistence(gitUserId);
            return new ResponseEntity<>(gitUser, HttpStatus.OK);
        } catch (DatabaseConnectionException d) {
            // Print exception and abort if database error occurs
            logger.error(d.getMessage(), d);
            return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException n) {
            return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * This method returns events from a gituser with a specific ID.
     *
     * @param gitUserId
     * @return json
     */
    @GetMapping(path = "/{gitUserId}/events", produces = "application/json")
    @ApiOperation(value = "Get commits")
    public ResponseEntity<?> getGitUserEvents(@PathVariable("gitUserId") Long gitUserId) throws GitHubAPIException, IOException, URISyntaxException {
        // Check if user exists in database
        GitUser gitUser = null;
        try {
            gitUser = gitUserService.checkGitUserExistence(gitUserId);
        } catch (DatabaseConnectionException d) {
            // Print exception and abort if database error occurs
            logger.error(d.getMessage(), d);
            return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException n) {
            return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
        }
        String json = null;
        json = grabber.getUserEvents(gitUser);
        return new ResponseEntity<>(json, HttpStatus.OK);
        }
    }
