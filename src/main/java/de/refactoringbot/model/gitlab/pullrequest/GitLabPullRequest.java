
package de.refactoringbot.model.gitlab.pullrequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.refactoringbot.model.gitlab.user.GitLabUser;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "iid",
    "project_id",
    "title",
    "description",
    "state",
    "created_at",
    "updated_at",
    "target_branch",
    "source_branch",
    "upvotes",
    "downvotes",
    "author",
    "user",
    "assignee",
    "source_project_id",
    "target_project_id",
    "labels",
    "work_in_progress",
    "milestone",
    "merge_when_pipeline_succeeds",
    "merge_status",
    "merge_error",
    "sha",
    "merge_commit_sha",
    "user_notes_count",
    "discussion_locked",
    "should_remove_source_branch",
    "force_remove_source_branch",
    "allow_collaboration",
    "allow_maintainer_to_push",
    "web_url",
    "time_stats",
    "squash",
    "subscribed",
    "changes_count",
    "merged_by",
    "merged_at",
    "closed_by",
    "closed_at",
    "latest_build_started_at",
    "latest_build_finished_at",
    "first_deployed_to_production_at",
    "pipeline",
    "diff_refs",
    "diverged_commits_count",
    "rebase_in_progress",
    "approvals_before_merge"
})
public class GitLabPullRequest {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("iid")
    private Integer iid;
    @JsonProperty("project_id")
    private Integer projectId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("state")
    private String state;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("target_branch")
    private String targetBranch;
    @JsonProperty("source_branch")
    private String sourceBranch;
    @JsonProperty("upvotes")
    private Integer upvotes;
    @JsonProperty("downvotes")
    private Integer downvotes;
    @JsonProperty("author")
    private GitLabUser author;
    @JsonProperty("user")
    private User user;
    @JsonProperty("assignee")
    private GitLabUser assignee;
    @JsonProperty("source_project_id")
    private Integer sourceProjectId;
    @JsonProperty("target_project_id")
    private Integer targetProjectId;
    @JsonProperty("labels")
    private List<String> labels = null;
    @JsonProperty("work_in_progress")
    private Boolean workInProgress;
    @JsonProperty("milestone")
    private Milestone milestone;
    @JsonProperty("merge_when_pipeline_succeeds")
    private Boolean mergeWhenPipelineSucceeds;
    @JsonProperty("merge_status")
    private String mergeStatus;
    @JsonProperty("merge_error")
    private Object mergeError;
    @JsonProperty("sha")
    private String sha;
    @JsonProperty("merge_commit_sha")
    private Object mergeCommitSha;
    @JsonProperty("user_notes_count")
    private Integer userNotesCount;
    @JsonProperty("discussion_locked")
    private Object discussionLocked;
    @JsonProperty("should_remove_source_branch")
    private Boolean shouldRemoveSourceBranch;
    @JsonProperty("force_remove_source_branch")
    private Boolean forceRemoveSourceBranch;
    @JsonProperty("allow_collaboration")
    private Boolean allowCollaboration;
    @JsonProperty("allow_maintainer_to_push")
    private Boolean allowMaintainerToPush;
    @JsonProperty("web_url")
    private String webUrl;
    @JsonProperty("time_stats")
    private TimeStats timeStats;
    @JsonProperty("squash")
    private Boolean squash;
    @JsonProperty("subscribed")
    private Boolean subscribed;
    @JsonProperty("changes_count")
    private String changesCount;
    @JsonProperty("merged_by")
    private GitLabUser mergedBy;
    @JsonProperty("merged_at")
    private String mergedAt;
    @JsonProperty("closed_by")
    private Object closedBy;
    @JsonProperty("closed_at")
    private Object closedAt;
    @JsonProperty("latest_build_started_at")
    private String latestBuildStartedAt;
    @JsonProperty("latest_build_finished_at")
    private String latestBuildFinishedAt;
    @JsonProperty("first_deployed_to_production_at")
    private Object firstDeployedToProductionAt;
    @JsonProperty("pipeline")
    private Pipeline pipeline;
    @JsonProperty("diff_refs")
    private DiffRefs diffRefs;
    @JsonProperty("diverged_commits_count")
    private Integer divergedCommitsCount;
    @JsonProperty("rebase_in_progress")
    private Boolean rebaseInProgress;
    @JsonProperty("approvals_before_merge")
    private Object approvalsBeforeMerge;
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

    @JsonProperty("iid")
    public Integer getIid() {
        return iid;
    }

    @JsonProperty("iid")
    public void setIid(Integer iid) {
        this.iid = iid;
    }

    @JsonProperty("project_id")
    public Integer getProjectId() {
        return projectId;
    }

    @JsonProperty("project_id")
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
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

    @JsonProperty("target_branch")
    public String getTargetBranch() {
        return targetBranch;
    }

    @JsonProperty("target_branch")
    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    @JsonProperty("source_branch")
    public String getSourceBranch() {
        return sourceBranch;
    }

    @JsonProperty("source_branch")
    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    @JsonProperty("upvotes")
    public Integer getUpvotes() {
        return upvotes;
    }

    @JsonProperty("upvotes")
    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    @JsonProperty("downvotes")
    public Integer getDownvotes() {
        return downvotes;
    }

    @JsonProperty("downvotes")
    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    @JsonProperty("author")
    public GitLabUser getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(GitLabUser author) {
        this.author = author;
    }

    @JsonProperty("user")
    public User getUser() {
        return user;
    }

    @JsonProperty("user")
    public void setUser(User user) {
        this.user = user;
    }

    @JsonProperty("assignee")
    public GitLabUser getAssignee() {
        return assignee;
    }

    @JsonProperty("assignee")
    public void setAssignee(GitLabUser assignee) {
        this.assignee = assignee;
    }

    @JsonProperty("source_project_id")
    public Integer getSourceProjectId() {
        return sourceProjectId;
    }

    @JsonProperty("source_project_id")
    public void setSourceProjectId(Integer sourceProjectId) {
        this.sourceProjectId = sourceProjectId;
    }

    @JsonProperty("target_project_id")
    public Integer getTargetProjectId() {
        return targetProjectId;
    }

    @JsonProperty("target_project_id")
    public void setTargetProjectId(Integer targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    @JsonProperty("labels")
    public List<String> getLabels() {
        return labels;
    }

    @JsonProperty("labels")
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @JsonProperty("work_in_progress")
    public Boolean getWorkInProgress() {
        return workInProgress;
    }

    @JsonProperty("work_in_progress")
    public void setWorkInProgress(Boolean workInProgress) {
        this.workInProgress = workInProgress;
    }

    @JsonProperty("milestone")
    public Milestone getMilestone() {
        return milestone;
    }

    @JsonProperty("milestone")
    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    @JsonProperty("merge_when_pipeline_succeeds")
    public Boolean getMergeWhenPipelineSucceeds() {
        return mergeWhenPipelineSucceeds;
    }

    @JsonProperty("merge_when_pipeline_succeeds")
    public void setMergeWhenPipelineSucceeds(Boolean mergeWhenPipelineSucceeds) {
        this.mergeWhenPipelineSucceeds = mergeWhenPipelineSucceeds;
    }

    @JsonProperty("merge_status")
    public String getMergeStatus() {
        return mergeStatus;
    }

    @JsonProperty("merge_status")
    public void setMergeStatus(String mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    @JsonProperty("merge_error")
    public Object getMergeError() {
        return mergeError;
    }

    @JsonProperty("merge_error")
    public void setMergeError(Object mergeError) {
        this.mergeError = mergeError;
    }

    @JsonProperty("sha")
    public String getSha() {
        return sha;
    }

    @JsonProperty("sha")
    public void setSha(String sha) {
        this.sha = sha;
    }

    @JsonProperty("merge_commit_sha")
    public Object getMergeCommitSha() {
        return mergeCommitSha;
    }

    @JsonProperty("merge_commit_sha")
    public void setMergeCommitSha(Object mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    @JsonProperty("user_notes_count")
    public Integer getUserNotesCount() {
        return userNotesCount;
    }

    @JsonProperty("user_notes_count")
    public void setUserNotesCount(Integer userNotesCount) {
        this.userNotesCount = userNotesCount;
    }

    @JsonProperty("discussion_locked")
    public Object getDiscussionLocked() {
        return discussionLocked;
    }

    @JsonProperty("discussion_locked")
    public void setDiscussionLocked(Object discussionLocked) {
        this.discussionLocked = discussionLocked;
    }

    @JsonProperty("should_remove_source_branch")
    public Boolean getShouldRemoveSourceBranch() {
        return shouldRemoveSourceBranch;
    }

    @JsonProperty("should_remove_source_branch")
    public void setShouldRemoveSourceBranch(Boolean shouldRemoveSourceBranch) {
        this.shouldRemoveSourceBranch = shouldRemoveSourceBranch;
    }

    @JsonProperty("force_remove_source_branch")
    public Boolean getForceRemoveSourceBranch() {
        return forceRemoveSourceBranch;
    }

    @JsonProperty("force_remove_source_branch")
    public void setForceRemoveSourceBranch(Boolean forceRemoveSourceBranch) {
        this.forceRemoveSourceBranch = forceRemoveSourceBranch;
    }

    @JsonProperty("allow_collaboration")
    public Boolean getAllowCollaboration() {
        return allowCollaboration;
    }

    @JsonProperty("allow_collaboration")
    public void setAllowCollaboration(Boolean allowCollaboration) {
        this.allowCollaboration = allowCollaboration;
    }

    @JsonProperty("allow_maintainer_to_push")
    public Boolean getAllowMaintainerToPush() {
        return allowMaintainerToPush;
    }

    @JsonProperty("allow_maintainer_to_push")
    public void setAllowMaintainerToPush(Boolean allowMaintainerToPush) {
        this.allowMaintainerToPush = allowMaintainerToPush;
    }

    @JsonProperty("web_url")
    public String getWebUrl() {
        return webUrl;
    }

    @JsonProperty("web_url")
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @JsonProperty("time_stats")
    public TimeStats getTimeStats() {
        return timeStats;
    }

    @JsonProperty("time_stats")
    public void setTimeStats(TimeStats timeStats) {
        this.timeStats = timeStats;
    }

    @JsonProperty("squash")
    public Boolean getSquash() {
        return squash;
    }

    @JsonProperty("squash")
    public void setSquash(Boolean squash) {
        this.squash = squash;
    }

    @JsonProperty("subscribed")
    public Boolean getSubscribed() {
        return subscribed;
    }

    @JsonProperty("subscribed")
    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    @JsonProperty("changes_count")
    public String getChangesCount() {
        return changesCount;
    }

    @JsonProperty("changes_count")
    public void setChangesCount(String changesCount) {
        this.changesCount = changesCount;
    }

    @JsonProperty("merged_by")
    public GitLabUser getMergedBy() {
        return mergedBy;
    }

    @JsonProperty("merged_by")
    public void setMergedBy(GitLabUser mergedBy) {
        this.mergedBy = mergedBy;
    }

    @JsonProperty("merged_at")
    public String getMergedAt() {
        return mergedAt;
    }

    @JsonProperty("merged_at")
    public void setMergedAt(String mergedAt) {
        this.mergedAt = mergedAt;
    }

    @JsonProperty("closed_by")
    public Object getClosedBy() {
        return closedBy;
    }

    @JsonProperty("closed_by")
    public void setClosedBy(Object closedBy) {
        this.closedBy = closedBy;
    }

    @JsonProperty("closed_at")
    public Object getClosedAt() {
        return closedAt;
    }

    @JsonProperty("closed_at")
    public void setClosedAt(Object closedAt) {
        this.closedAt = closedAt;
    }

    @JsonProperty("latest_build_started_at")
    public String getLatestBuildStartedAt() {
        return latestBuildStartedAt;
    }

    @JsonProperty("latest_build_started_at")
    public void setLatestBuildStartedAt(String latestBuildStartedAt) {
        this.latestBuildStartedAt = latestBuildStartedAt;
    }

    @JsonProperty("latest_build_finished_at")
    public String getLatestBuildFinishedAt() {
        return latestBuildFinishedAt;
    }

    @JsonProperty("latest_build_finished_at")
    public void setLatestBuildFinishedAt(String latestBuildFinishedAt) {
        this.latestBuildFinishedAt = latestBuildFinishedAt;
    }

    @JsonProperty("first_deployed_to_production_at")
    public Object getFirstDeployedToProductionAt() {
        return firstDeployedToProductionAt;
    }

    @JsonProperty("first_deployed_to_production_at")
    public void setFirstDeployedToProductionAt(Object firstDeployedToProductionAt) {
        this.firstDeployedToProductionAt = firstDeployedToProductionAt;
    }

    @JsonProperty("pipeline")
    public Pipeline getPipeline() {
        return pipeline;
    }

    @JsonProperty("pipeline")
    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    @JsonProperty("diff_refs")
    public DiffRefs getDiffRefs() {
        return diffRefs;
    }

    @JsonProperty("diff_refs")
    public void setDiffRefs(DiffRefs diffRefs) {
        this.diffRefs = diffRefs;
    }

    @JsonProperty("diverged_commits_count")
    public Integer getDivergedCommitsCount() {
        return divergedCommitsCount;
    }

    @JsonProperty("diverged_commits_count")
    public void setDivergedCommitsCount(Integer divergedCommitsCount) {
        this.divergedCommitsCount = divergedCommitsCount;
    }

    @JsonProperty("rebase_in_progress")
    public Boolean getRebaseInProgress() {
        return rebaseInProgress;
    }

    @JsonProperty("rebase_in_progress")
    public void setRebaseInProgress(Boolean rebaseInProgress) {
        this.rebaseInProgress = rebaseInProgress;
    }

    @JsonProperty("approvals_before_merge")
    public Object getApprovalsBeforeMerge() {
        return approvalsBeforeMerge;
    }

    @JsonProperty("approvals_before_merge")
    public void setApprovalsBeforeMerge(Object approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
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
