
package de.refactoringbot.model.github.pullrequestcomment;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.refactoringbot.model.github.user.GithubUser;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "url",
    "pull_request_review_id",
    "id",
    "node_id",
    "diff_hunk",
    "path",
    "position",
    "original_position",
    "commit_id",
    "original_commit_id",
    "user",
    "body",
    "created_at",
    "updated_at",
    "html_url",
    "pull_request_url",
    "author_association",
    "_links"
})
public class PullRequestComment {

    @JsonProperty("url")
    private String url;
    @JsonProperty("pull_request_review_id")
    private Integer pullRequestReviewId;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("node_id")
    private String nodeId;
    @JsonProperty("diff_hunk")
    private String diffHunk;
    @JsonProperty("path")
    private String path;
    @JsonProperty("position")
    private Integer position;
    @JsonProperty("original_position")
    private Integer originalPosition;
    @JsonProperty("commit_id")
    private String commitId;
    @JsonProperty("original_commit_id")
    private String originalCommitId;
    @JsonProperty("user")
    private GithubUser user;
    @JsonProperty("body")
    private String body;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("pull_request_url")
    private String pullRequestUrl;
    @JsonProperty("author_association")
    private String authorAssociation;
    @JsonProperty("_links")
    private Links links;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("pull_request_review_id")
    public Integer getPullRequestReviewId() {
        return pullRequestReviewId;
    }

    @JsonProperty("pull_request_review_id")
    public void setPullRequestReviewId(Integer pullRequestReviewId) {
        this.pullRequestReviewId = pullRequestReviewId;
    }

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

    @JsonProperty("diff_hunk")
    public String getDiffHunk() {
        return diffHunk;
    }

    @JsonProperty("diff_hunk")
    public void setDiffHunk(String diffHunk) {
        this.diffHunk = diffHunk;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("position")
    public Integer getPosition() {
        return position;
    }

    @JsonProperty("position")
    public void setPosition(Integer position) {
        this.position = position;
    }

    @JsonProperty("original_position")
    public Integer getOriginalPosition() {
        return originalPosition;
    }

    @JsonProperty("original_position")
    public void setOriginalPosition(Integer originalPosition) {
        this.originalPosition = originalPosition;
    }

    @JsonProperty("commit_id")
    public String getCommitId() {
        return commitId;
    }

    @JsonProperty("commit_id")
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    @JsonProperty("original_commit_id")
    public String getOriginalCommitId() {
        return originalCommitId;
    }

    @JsonProperty("original_commit_id")
    public void setOriginalCommitId(String originalCommitId) {
        this.originalCommitId = originalCommitId;
    }

    @JsonProperty("user")
    public GithubUser getUser() {
        return user;
    }

    @JsonProperty("user")
    public void setUser(GithubUser user) {
        this.user = user;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(String body) {
        this.body = body;
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

    @JsonProperty("html_url")
    public String getHtmlUrl() {
        return htmlUrl;
    }

    @JsonProperty("html_url")
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @JsonProperty("pull_request_url")
    public String getPullRequestUrl() {
        return pullRequestUrl;
    }

    @JsonProperty("pull_request_url")
    public void setPullRequestUrl(String pullRequestUrl) {
        this.pullRequestUrl = pullRequestUrl;
    }

    @JsonProperty("author_association")
    public String getAuthorAssociation() {
        return authorAssociation;
    }

    @JsonProperty("author_association")
    public void setAuthorAssociation(String authorAssociation) {
        this.authorAssociation = authorAssociation;
    }

    @JsonProperty("_links")
    public Links getLinks() {
        return links;
    }

    @JsonProperty("_links")
    public void setLinks(Links links) {
        this.links = links;
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
