package de.refactoringbot.model.gituser;

import de.refactoringbot.model.configuration.FileHoster;

import javax.persistence.*;

@Entity
@Table(name = "GIT_USERS")
public class GitUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gitUserId;
    private String gitUserName;
    private String gitUserEmail;
    private String gitUserToken;

    
    @Enumerated(EnumType.STRING)
    private FileHoster repoService;

    public Long getGitUserId() {
        return gitUserId;
    }

    public String getGitUserName() {
        return gitUserName;
    }

    public void setGitUserName(String gitUserName) {
        this.gitUserName = gitUserName;
    }

    public String getGitUserToken() {
        return gitUserToken;
    }

    public void setGitUserToken(String gitUserToken) {
        this.gitUserToken = gitUserToken;
    }

    public String getGitUserEmail() {
        return gitUserEmail;
    }

    public void setGitUserEmail(String gitUserEmail) {
        this.gitUserEmail = gitUserEmail;
    }

    public FileHoster getRepoService() {
        return repoService;
    }

    public void setRepoService(FileHoster repoService) {
        this.repoService = repoService;
    }

}
