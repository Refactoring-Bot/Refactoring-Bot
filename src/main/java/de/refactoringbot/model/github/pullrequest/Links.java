
package de.refactoringbot.model.github.pullrequest;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.refactoringbot.model.github.shared.Html;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "self",
    "html",
    "issue",
    "comments",
    "review_comments",
    "review_comment",
    "commits",
    "statuses"
})
public class Links {

    @JsonProperty("self")
    private Html self;
    @JsonProperty("html")
    private Html html;
    @JsonProperty("issue")
    private Html issue;
    @JsonProperty("comments")
    private Html comments;
    @JsonProperty("review_comments")
    private Html reviewComments;
    @JsonProperty("review_comment")
    private Html reviewComment;
    @JsonProperty("commits")
    private Html commits;
    @JsonProperty("statuses")
    private Html statuses;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("self")
    public Html getSelf() {
        return self;
    }

    @JsonProperty("self")
    public void setSelf(Html self) {
        this.self = self;
    }

    @JsonProperty("html")
    public Html getHtml() {
        return html;
    }

    @JsonProperty("html")
    public void setHtml(Html html) {
        this.html = html;
    }

    @JsonProperty("issue")
    public Html getIssue() {
        return issue;
    }

    @JsonProperty("issue")
    public void setIssue(Html issue) {
        this.issue = issue;
    }

    @JsonProperty("comments")
    public Html getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(Html comments) {
        this.comments = comments;
    }

    @JsonProperty("review_comments")
    public Html getReviewComments() {
        return reviewComments;
    }

    @JsonProperty("review_comments")
    public void setReviewComments(Html reviewComments) {
        this.reviewComments = reviewComments;
    }

    @JsonProperty("review_comment")
    public Html getReviewComment() {
        return reviewComment;
    }

    @JsonProperty("review_comment")
    public void setReviewComment(Html reviewComment) {
        this.reviewComment = reviewComment;
    }

    @JsonProperty("commits")
    public Html getCommits() {
        return commits;
    }

    @JsonProperty("commits")
    public void setCommits(Html commits) {
        this.commits = commits;
    }

    @JsonProperty("statuses")
    public Html getStatuses() {
        return statuses;
    }

    @JsonProperty("statuses")
    public void setStatuses(Html statuses) {
        this.statuses = statuses;
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
