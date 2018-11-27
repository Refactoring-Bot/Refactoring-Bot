
package de.BA.refactoringBot.model.githubModels.pullRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "node_id",
    "name",
    "full_name",
    "owner",
    "private",
    "html_url",
    "description",
    "fork",
    "url",
    "archive_url",
    "assignees_url",
    "blobs_url",
    "branches_url",
    "collaborators_url",
    "comments_url",
    "commits_url",
    "compare_url",
    "contents_url",
    "contributors_url",
    "deployments_url",
    "downloads_url",
    "events_url",
    "forks_url",
    "git_commits_url",
    "git_refs_url",
    "git_tags_url",
    "git_url",
    "issue_comment_url",
    "issue_events_url",
    "issues_url",
    "keys_url",
    "labels_url",
    "languages_url",
    "merges_url",
    "milestones_url",
    "notifications_url",
    "pulls_url",
    "releases_url",
    "ssh_url",
    "stargazers_url",
    "statuses_url",
    "subscribers_url",
    "subscription_url",
    "tags_url",
    "teams_url",
    "trees_url",
    "clone_url",
    "mirror_url",
    "hooks_url",
    "svn_url",
    "homepage",
    "language",
    "forks_count",
    "stargazers_count",
    "watchers_count",
    "size",
    "default_branch",
    "open_issues_count",
    "topics",
    "has_issues",
    "has_projects",
    "has_wiki",
    "has_pages",
    "has_downloads",
    "archived",
    "pushed_at",
    "created_at",
    "updated_at",
    "permissions",
    "allow_rebase_merge",
    "allow_squash_merge",
    "allow_merge_commit",
    "subscribers_count",
    "network_count"
})
public class Repo {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("node_id")
    private String nodeId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("owner")
    private Owner owner;
    @JsonProperty("private")
    private Boolean _private;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("description")
    private String description;
    @JsonProperty("fork")
    private Boolean fork;
    @JsonProperty("url")
    private String url;
    @JsonProperty("archive_url")
    private String archiveUrl;
    @JsonProperty("assignees_url")
    private String assigneesUrl;
    @JsonProperty("blobs_url")
    private String blobsUrl;
    @JsonProperty("branches_url")
    private String branchesUrl;
    @JsonProperty("collaborators_url")
    private String collaboratorsUrl;
    @JsonProperty("comments_url")
    private String commentsUrl;
    @JsonProperty("commits_url")
    private String commitsUrl;
    @JsonProperty("compare_url")
    private String compareUrl;
    @JsonProperty("contents_url")
    private String contentsUrl;
    @JsonProperty("contributors_url")
    private String contributorsUrl;
    @JsonProperty("deployments_url")
    private String deploymentsUrl;
    @JsonProperty("downloads_url")
    private String downloadsUrl;
    @JsonProperty("events_url")
    private String eventsUrl;
    @JsonProperty("forks_url")
    private String forksUrl;
    @JsonProperty("git_commits_url")
    private String gitCommitsUrl;
    @JsonProperty("git_refs_url")
    private String gitRefsUrl;
    @JsonProperty("git_tags_url")
    private String gitTagsUrl;
    @JsonProperty("git_url")
    private String gitUrl;
    @JsonProperty("issue_comment_url")
    private String issueCommentUrl;
    @JsonProperty("issue_events_url")
    private String issueEventsUrl;
    @JsonProperty("issues_url")
    private String issuesUrl;
    @JsonProperty("keys_url")
    private String keysUrl;
    @JsonProperty("labels_url")
    private String labelsUrl;
    @JsonProperty("languages_url")
    private String languagesUrl;
    @JsonProperty("merges_url")
    private String mergesUrl;
    @JsonProperty("milestones_url")
    private String milestonesUrl;
    @JsonProperty("notifications_url")
    private String notificationsUrl;
    @JsonProperty("pulls_url")
    private String pullsUrl;
    @JsonProperty("releases_url")
    private String releasesUrl;
    @JsonProperty("ssh_url")
    private String sshUrl;
    @JsonProperty("stargazers_url")
    private String stargazersUrl;
    @JsonProperty("statuses_url")
    private String statusesUrl;
    @JsonProperty("subscribers_url")
    private String subscribersUrl;
    @JsonProperty("subscription_url")
    private String subscriptionUrl;
    @JsonProperty("tags_url")
    private String tagsUrl;
    @JsonProperty("teams_url")
    private String teamsUrl;
    @JsonProperty("trees_url")
    private String treesUrl;
    @JsonProperty("clone_url")
    private String cloneUrl;
    @JsonProperty("mirror_url")
    private String mirrorUrl;
    @JsonProperty("hooks_url")
    private String hooksUrl;
    @JsonProperty("svn_url")
    private String svnUrl;
    @JsonProperty("homepage")
    private String homepage;
    @JsonProperty("language")
    private Object language;
    @JsonProperty("forks_count")
    private Integer forksCount;
    @JsonProperty("stargazers_count")
    private Integer stargazersCount;
    @JsonProperty("watchers_count")
    private Integer watchersCount;
    @JsonProperty("size")
    private Integer size;
    @JsonProperty("default_branch")
    private String defaultBranch;
    @JsonProperty("open_issues_count")
    private Integer openIssuesCount;
    @JsonProperty("topics")
    private List<String> topics = null;
    @JsonProperty("has_issues")
    private Boolean hasIssues;
    @JsonProperty("has_projects")
    private Boolean hasProjects;
    @JsonProperty("has_wiki")
    private Boolean hasWiki;
    @JsonProperty("has_pages")
    private Boolean hasPages;
    @JsonProperty("has_downloads")
    private Boolean hasDownloads;
    @JsonProperty("archived")
    private Boolean archived;
    @JsonProperty("pushed_at")
    private String pushedAt;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("permissions")
    private Permissions permissions;
    @JsonProperty("allow_rebase_merge")
    private Boolean allowRebaseMerge;
    @JsonProperty("allow_squash_merge")
    private Boolean allowSquashMerge;
    @JsonProperty("allow_merge_commit")
    private Boolean allowMergeCommit;
    @JsonProperty("subscribers_count")
    private Integer subscribersCount;
    @JsonProperty("network_count")
    private Integer networkCount;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("node_id")
    public String getNodeId() {
        return nodeId;
    }

    @JsonProperty("node_id")
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("full_name")
    public String getFullName() {
        return fullName;
    }

    @JsonProperty("full_name")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @JsonProperty("owner")
    public Owner getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @JsonProperty("private")
    public Boolean getPrivate() {
        return _private;
    }

    @JsonProperty("private")
    public void setPrivate(Boolean _private) {
        this._private = _private;
    }

    @JsonProperty("html_url")
    public String getHtmlUrl() {
        return htmlUrl;
    }

    @JsonProperty("html_url")
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("fork")
    public Boolean getFork() {
        return fork;
    }

    @JsonProperty("fork")
    public void setFork(Boolean fork) {
        this.fork = fork;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("archive_url")
    public String getArchiveUrl() {
        return archiveUrl;
    }

    @JsonProperty("archive_url")
    public void setArchiveUrl(String archiveUrl) {
        this.archiveUrl = archiveUrl;
    }

    @JsonProperty("assignees_url")
    public String getAssigneesUrl() {
        return assigneesUrl;
    }

    @JsonProperty("assignees_url")
    public void setAssigneesUrl(String assigneesUrl) {
        this.assigneesUrl = assigneesUrl;
    }

    @JsonProperty("blobs_url")
    public String getBlobsUrl() {
        return blobsUrl;
    }

    @JsonProperty("blobs_url")
    public void setBlobsUrl(String blobsUrl) {
        this.blobsUrl = blobsUrl;
    }

    @JsonProperty("branches_url")
    public String getBranchesUrl() {
        return branchesUrl;
    }

    @JsonProperty("branches_url")
    public void setBranchesUrl(String branchesUrl) {
        this.branchesUrl = branchesUrl;
    }

    @JsonProperty("collaborators_url")
    public String getCollaboratorsUrl() {
        return collaboratorsUrl;
    }

    @JsonProperty("collaborators_url")
    public void setCollaboratorsUrl(String collaboratorsUrl) {
        this.collaboratorsUrl = collaboratorsUrl;
    }

    @JsonProperty("comments_url")
    public String getCommentsUrl() {
        return commentsUrl;
    }

    @JsonProperty("comments_url")
    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }

    @JsonProperty("commits_url")
    public String getCommitsUrl() {
        return commitsUrl;
    }

    @JsonProperty("commits_url")
    public void setCommitsUrl(String commitsUrl) {
        this.commitsUrl = commitsUrl;
    }

    @JsonProperty("compare_url")
    public String getCompareUrl() {
        return compareUrl;
    }

    @JsonProperty("compare_url")
    public void setCompareUrl(String compareUrl) {
        this.compareUrl = compareUrl;
    }

    @JsonProperty("contents_url")
    public String getContentsUrl() {
        return contentsUrl;
    }

    @JsonProperty("contents_url")
    public void setContentsUrl(String contentsUrl) {
        this.contentsUrl = contentsUrl;
    }

    @JsonProperty("contributors_url")
    public String getContributorsUrl() {
        return contributorsUrl;
    }

    @JsonProperty("contributors_url")
    public void setContributorsUrl(String contributorsUrl) {
        this.contributorsUrl = contributorsUrl;
    }

    @JsonProperty("deployments_url")
    public String getDeploymentsUrl() {
        return deploymentsUrl;
    }

    @JsonProperty("deployments_url")
    public void setDeploymentsUrl(String deploymentsUrl) {
        this.deploymentsUrl = deploymentsUrl;
    }

    @JsonProperty("downloads_url")
    public String getDownloadsUrl() {
        return downloadsUrl;
    }

    @JsonProperty("downloads_url")
    public void setDownloadsUrl(String downloadsUrl) {
        this.downloadsUrl = downloadsUrl;
    }

    @JsonProperty("events_url")
    public String getEventsUrl() {
        return eventsUrl;
    }

    @JsonProperty("events_url")
    public void setEventsUrl(String eventsUrl) {
        this.eventsUrl = eventsUrl;
    }

    @JsonProperty("forks_url")
    public String getForksUrl() {
        return forksUrl;
    }

    @JsonProperty("forks_url")
    public void setForksUrl(String forksUrl) {
        this.forksUrl = forksUrl;
    }

    @JsonProperty("git_commits_url")
    public String getGitCommitsUrl() {
        return gitCommitsUrl;
    }

    @JsonProperty("git_commits_url")
    public void setGitCommitsUrl(String gitCommitsUrl) {
        this.gitCommitsUrl = gitCommitsUrl;
    }

    @JsonProperty("git_refs_url")
    public String getGitRefsUrl() {
        return gitRefsUrl;
    }

    @JsonProperty("git_refs_url")
    public void setGitRefsUrl(String gitRefsUrl) {
        this.gitRefsUrl = gitRefsUrl;
    }

    @JsonProperty("git_tags_url")
    public String getGitTagsUrl() {
        return gitTagsUrl;
    }

    @JsonProperty("git_tags_url")
    public void setGitTagsUrl(String gitTagsUrl) {
        this.gitTagsUrl = gitTagsUrl;
    }

    @JsonProperty("git_url")
    public String getGitUrl() {
        return gitUrl;
    }

    @JsonProperty("git_url")
    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    @JsonProperty("issue_comment_url")
    public String getIssueCommentUrl() {
        return issueCommentUrl;
    }

    @JsonProperty("issue_comment_url")
    public void setIssueCommentUrl(String issueCommentUrl) {
        this.issueCommentUrl = issueCommentUrl;
    }

    @JsonProperty("issue_events_url")
    public String getIssueEventsUrl() {
        return issueEventsUrl;
    }

    @JsonProperty("issue_events_url")
    public void setIssueEventsUrl(String issueEventsUrl) {
        this.issueEventsUrl = issueEventsUrl;
    }

    @JsonProperty("issues_url")
    public String getIssuesUrl() {
        return issuesUrl;
    }

    @JsonProperty("issues_url")
    public void setIssuesUrl(String issuesUrl) {
        this.issuesUrl = issuesUrl;
    }

    @JsonProperty("keys_url")
    public String getKeysUrl() {
        return keysUrl;
    }

    @JsonProperty("keys_url")
    public void setKeysUrl(String keysUrl) {
        this.keysUrl = keysUrl;
    }

    @JsonProperty("labels_url")
    public String getLabelsUrl() {
        return labelsUrl;
    }

    @JsonProperty("labels_url")
    public void setLabelsUrl(String labelsUrl) {
        this.labelsUrl = labelsUrl;
    }

    @JsonProperty("languages_url")
    public String getLanguagesUrl() {
        return languagesUrl;
    }

    @JsonProperty("languages_url")
    public void setLanguagesUrl(String languagesUrl) {
        this.languagesUrl = languagesUrl;
    }

    @JsonProperty("merges_url")
    public String getMergesUrl() {
        return mergesUrl;
    }

    @JsonProperty("merges_url")
    public void setMergesUrl(String mergesUrl) {
        this.mergesUrl = mergesUrl;
    }

    @JsonProperty("milestones_url")
    public String getMilestonesUrl() {
        return milestonesUrl;
    }

    @JsonProperty("milestones_url")
    public void setMilestonesUrl(String milestonesUrl) {
        this.milestonesUrl = milestonesUrl;
    }

    @JsonProperty("notifications_url")
    public String getNotificationsUrl() {
        return notificationsUrl;
    }

    @JsonProperty("notifications_url")
    public void setNotificationsUrl(String notificationsUrl) {
        this.notificationsUrl = notificationsUrl;
    }

    @JsonProperty("pulls_url")
    public String getPullsUrl() {
        return pullsUrl;
    }

    @JsonProperty("pulls_url")
    public void setPullsUrl(String pullsUrl) {
        this.pullsUrl = pullsUrl;
    }

    @JsonProperty("releases_url")
    public String getReleasesUrl() {
        return releasesUrl;
    }

    @JsonProperty("releases_url")
    public void setReleasesUrl(String releasesUrl) {
        this.releasesUrl = releasesUrl;
    }

    @JsonProperty("ssh_url")
    public String getSshUrl() {
        return sshUrl;
    }

    @JsonProperty("ssh_url")
    public void setSshUrl(String sshUrl) {
        this.sshUrl = sshUrl;
    }

    @JsonProperty("stargazers_url")
    public String getStargazersUrl() {
        return stargazersUrl;
    }

    @JsonProperty("stargazers_url")
    public void setStargazersUrl(String stargazersUrl) {
        this.stargazersUrl = stargazersUrl;
    }

    @JsonProperty("statuses_url")
    public String getStatusesUrl() {
        return statusesUrl;
    }

    @JsonProperty("statuses_url")
    public void setStatusesUrl(String statusesUrl) {
        this.statusesUrl = statusesUrl;
    }

    @JsonProperty("subscribers_url")
    public String getSubscribersUrl() {
        return subscribersUrl;
    }

    @JsonProperty("subscribers_url")
    public void setSubscribersUrl(String subscribersUrl) {
        this.subscribersUrl = subscribersUrl;
    }

    @JsonProperty("subscription_url")
    public String getSubscriptionUrl() {
        return subscriptionUrl;
    }

    @JsonProperty("subscription_url")
    public void setSubscriptionUrl(String subscriptionUrl) {
        this.subscriptionUrl = subscriptionUrl;
    }

    @JsonProperty("tags_url")
    public String getTagsUrl() {
        return tagsUrl;
    }

    @JsonProperty("tags_url")
    public void setTagsUrl(String tagsUrl) {
        this.tagsUrl = tagsUrl;
    }

    @JsonProperty("teams_url")
    public String getTeamsUrl() {
        return teamsUrl;
    }

    @JsonProperty("teams_url")
    public void setTeamsUrl(String teamsUrl) {
        this.teamsUrl = teamsUrl;
    }

    @JsonProperty("trees_url")
    public String getTreesUrl() {
        return treesUrl;
    }

    @JsonProperty("trees_url")
    public void setTreesUrl(String treesUrl) {
        this.treesUrl = treesUrl;
    }

    @JsonProperty("clone_url")
    public String getCloneUrl() {
        return cloneUrl;
    }

    @JsonProperty("clone_url")
    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    @JsonProperty("mirror_url")
    public String getMirrorUrl() {
        return mirrorUrl;
    }

    @JsonProperty("mirror_url")
    public void setMirrorUrl(String mirrorUrl) {
        this.mirrorUrl = mirrorUrl;
    }

    @JsonProperty("hooks_url")
    public String getHooksUrl() {
        return hooksUrl;
    }

    @JsonProperty("hooks_url")
    public void setHooksUrl(String hooksUrl) {
        this.hooksUrl = hooksUrl;
    }

    @JsonProperty("svn_url")
    public String getSvnUrl() {
        return svnUrl;
    }

    @JsonProperty("svn_url")
    public void setSvnUrl(String svnUrl) {
        this.svnUrl = svnUrl;
    }

    @JsonProperty("homepage")
    public String getHomepage() {
        return homepage;
    }

    @JsonProperty("homepage")
    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    @JsonProperty("language")
    public Object getLanguage() {
        return language;
    }

    @JsonProperty("language")
    public void setLanguage(Object language) {
        this.language = language;
    }

    @JsonProperty("forks_count")
    public Integer getForksCount() {
        return forksCount;
    }

    @JsonProperty("forks_count")
    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
    }

    @JsonProperty("stargazers_count")
    public Integer getStargazersCount() {
        return stargazersCount;
    }

    @JsonProperty("stargazers_count")
    public void setStargazersCount(Integer stargazersCount) {
        this.stargazersCount = stargazersCount;
    }

    @JsonProperty("watchers_count")
    public Integer getWatchersCount() {
        return watchersCount;
    }

    @JsonProperty("watchers_count")
    public void setWatchersCount(Integer watchersCount) {
        this.watchersCount = watchersCount;
    }

    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

    @JsonProperty("default_branch")
    public String getDefaultBranch() {
        return defaultBranch;
    }

    @JsonProperty("default_branch")
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    @JsonProperty("open_issues_count")
    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    @JsonProperty("open_issues_count")
    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    @JsonProperty("topics")
    public List<String> getTopics() {
        return topics;
    }

    @JsonProperty("topics")
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    @JsonProperty("has_issues")
    public Boolean getHasIssues() {
        return hasIssues;
    }

    @JsonProperty("has_issues")
    public void setHasIssues(Boolean hasIssues) {
        this.hasIssues = hasIssues;
    }

    @JsonProperty("has_projects")
    public Boolean getHasProjects() {
        return hasProjects;
    }

    @JsonProperty("has_projects")
    public void setHasProjects(Boolean hasProjects) {
        this.hasProjects = hasProjects;
    }

    @JsonProperty("has_wiki")
    public Boolean getHasWiki() {
        return hasWiki;
    }

    @JsonProperty("has_wiki")
    public void setHasWiki(Boolean hasWiki) {
        this.hasWiki = hasWiki;
    }

    @JsonProperty("has_pages")
    public Boolean getHasPages() {
        return hasPages;
    }

    @JsonProperty("has_pages")
    public void setHasPages(Boolean hasPages) {
        this.hasPages = hasPages;
    }

    @JsonProperty("has_downloads")
    public Boolean getHasDownloads() {
        return hasDownloads;
    }

    @JsonProperty("has_downloads")
    public void setHasDownloads(Boolean hasDownloads) {
        this.hasDownloads = hasDownloads;
    }

    @JsonProperty("archived")
    public Boolean getArchived() {
        return archived;
    }

    @JsonProperty("archived")
    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    @JsonProperty("pushed_at")
    public String getPushedAt() {
        return pushedAt;
    }

    @JsonProperty("pushed_at")
    public void setPushedAt(String pushedAt) {
        this.pushedAt = pushedAt;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("permissions")
    public Permissions getPermissions() {
        return permissions;
    }

    @JsonProperty("permissions")
    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("allow_rebase_merge")
    public Boolean getAllowRebaseMerge() {
        return allowRebaseMerge;
    }

    @JsonProperty("allow_rebase_merge")
    public void setAllowRebaseMerge(Boolean allowRebaseMerge) {
        this.allowRebaseMerge = allowRebaseMerge;
    }

    @JsonProperty("allow_squash_merge")
    public Boolean getAllowSquashMerge() {
        return allowSquashMerge;
    }

    @JsonProperty("allow_squash_merge")
    public void setAllowSquashMerge(Boolean allowSquashMerge) {
        this.allowSquashMerge = allowSquashMerge;
    }

    @JsonProperty("allow_merge_commit")
    public Boolean getAllowMergeCommit() {
        return allowMergeCommit;
    }

    @JsonProperty("allow_merge_commit")
    public void setAllowMergeCommit(Boolean allowMergeCommit) {
        this.allowMergeCommit = allowMergeCommit;
    }

    @JsonProperty("subscribers_count")
    public Integer getSubscribersCount() {
        return subscribersCount;
    }

    @JsonProperty("subscribers_count")
    public void setSubscribersCount(Integer subscribersCount) {
        this.subscribersCount = subscribersCount;
    }

    @JsonProperty("network_count")
    public Integer getNetworkCount() {
        return networkCount;
    }

    @JsonProperty("network_count")
    public void setNetworkCount(Integer networkCount) {
        this.networkCount = networkCount;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
