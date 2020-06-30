package de.refactoringbot.model.gituser;

import de.refactoringbot.model.configuration.FileHoster;

public class GitUserDTO {


    private String gitUserName;
    private String gitUserEmail;
    private String gitUserToken;
    private FileHoster repoService;
    private String filehosterApiLink;

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

    public String getFilehosterApiLink() {
        return filehosterApiLink;
    }

    public void setFilehosterApiLink(String filehosterApiLink) {
        this.filehosterApiLink = filehosterApiLink;
    }

}
