
package de.refactoringbot.model.gitlab.repository;

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
    "description",
    "default_branch",
    "visibility",
    "ssh_url_to_repo",
    "http_url_to_repo",
    "web_url",
    "readme_url",
    "tag_list",
    "owner",
    "name",
    "name_with_namespace",
    "path",
    "path_with_namespace",
    "issues_enabled",
    "open_issues_count",
    "merge_requests_enabled",
    "jobs_enabled",
    "wiki_enabled",
    "snippets_enabled",
    "resolve_outdated_diff_discussions",
    "container_registry_enabled",
    "created_at",
    "last_activity_at",
    "creator_id",
    "namespace",
    "import_status",
    "import_error",
    "permissions",
    "archived",
    "avatar_url",
    "license_url",
    "license",
    "shared_runners_enabled",
    "forks_count",
    "star_count",
    "runners_token",
    "public_jobs",
    "shared_with_groups",
    "repository_storage",
    "only_allow_merge_if_pipeline_succeeds",
    "only_allow_merge_if_all_discussions_are_resolved",
    "printing_merge_requests_link_enabled",
    "request_access_enabled",
    "merge_method",
    "approvals_before_merge",
    "statistics",
    "_links"
})
public class GitLabRepository {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("description")
    private Object description;
    @JsonProperty("default_branch")
    private String defaultBranch;
    @JsonProperty("visibility")
    private String visibility;
    @JsonProperty("ssh_url_to_repo")
    private String sshUrlToRepo;
    @JsonProperty("http_url_to_repo")
    private String httpUrlToRepo;
    @JsonProperty("web_url")
    private String webUrl;
    @JsonProperty("readme_url")
    private String readmeUrl;
    @JsonProperty("tag_list")
    private List<String> tagList = null;
    @JsonProperty("owner")
    private Owner owner;
    @JsonProperty("name")
    private String name;
    @JsonProperty("name_with_namespace")
    private String nameWithNamespace;
    @JsonProperty("path")
    private String path;
    @JsonProperty("path_with_namespace")
    private String pathWithNamespace;
    @JsonProperty("issues_enabled")
    private Boolean issuesEnabled;
    @JsonProperty("open_issues_count")
    private Integer openIssuesCount;
    @JsonProperty("merge_requests_enabled")
    private Boolean mergeRequestsEnabled;
    @JsonProperty("jobs_enabled")
    private Boolean jobsEnabled;
    @JsonProperty("wiki_enabled")
    private Boolean wikiEnabled;
    @JsonProperty("snippets_enabled")
    private Boolean snippetsEnabled;
    @JsonProperty("resolve_outdated_diff_discussions")
    private Boolean resolveOutdatedDiffDiscussions;
    @JsonProperty("container_registry_enabled")
    private Boolean containerRegistryEnabled;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("last_activity_at")
    private String lastActivityAt;
    @JsonProperty("creator_id")
    private Integer creatorId;
    @JsonProperty("namespace")
    private Namespace namespace;
    @JsonProperty("import_status")
    private String importStatus;
    @JsonProperty("import_error")
    private Object importError;
    @JsonProperty("permissions")
    private Permissions permissions;
    @JsonProperty("archived")
    private Boolean archived;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("license_url")
    private String licenseUrl;
    @JsonProperty("license")
    private License license;
    @JsonProperty("shared_runners_enabled")
    private Boolean sharedRunnersEnabled;
    @JsonProperty("forks_count")
    private Integer forksCount;
    @JsonProperty("star_count")
    private Integer starCount;
    @JsonProperty("runners_token")
    private String runnersToken;
    @JsonProperty("public_jobs")
    private Boolean publicJobs;
    @JsonProperty("shared_with_groups")
    private List<SharedWithGroup> sharedWithGroups = null;
    @JsonProperty("repository_storage")
    private String repositoryStorage;
    @JsonProperty("only_allow_merge_if_pipeline_succeeds")
    private Boolean onlyAllowMergeIfPipelineSucceeds;
    @JsonProperty("only_allow_merge_if_all_discussions_are_resolved")
    private Boolean onlyAllowMergeIfAllDiscussionsAreResolved;
    @JsonProperty("printing_merge_requests_link_enabled")
    private Boolean printingMergeRequestsLinkEnabled;
    @JsonProperty("request_access_enabled")
    private Boolean requestAccessEnabled;
    @JsonProperty("merge_method")
    private String mergeMethod;
    @JsonProperty("approvals_before_merge")
    private Integer approvalsBeforeMerge;
    @JsonProperty("statistics")
    private Statistics statistics;
    @JsonProperty("_links")
    private Links links;
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

    @JsonProperty("description")
    public Object getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(Object description) {
        this.description = description;
    }

    @JsonProperty("default_branch")
    public String getDefaultBranch() {
        return defaultBranch;
    }

    @JsonProperty("default_branch")
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    @JsonProperty("visibility")
    public String getVisibility() {
        return visibility;
    }

    @JsonProperty("visibility")
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    @JsonProperty("ssh_url_to_repo")
    public String getSshUrlToRepo() {
        return sshUrlToRepo;
    }

    @JsonProperty("ssh_url_to_repo")
    public void setSshUrlToRepo(String sshUrlToRepo) {
        this.sshUrlToRepo = sshUrlToRepo;
    }

    @JsonProperty("http_url_to_repo")
    public String getHttpUrlToRepo() {
        return httpUrlToRepo;
    }

    @JsonProperty("http_url_to_repo")
    public void setHttpUrlToRepo(String httpUrlToRepo) {
        this.httpUrlToRepo = httpUrlToRepo;
    }

    @JsonProperty("web_url")
    public String getWebUrl() {
        return webUrl;
    }

    @JsonProperty("web_url")
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @JsonProperty("readme_url")
    public String getReadmeUrl() {
        return readmeUrl;
    }

    @JsonProperty("readme_url")
    public void setReadmeUrl(String readmeUrl) {
        this.readmeUrl = readmeUrl;
    }

    @JsonProperty("tag_list")
    public List<String> getTagList() {
        return tagList;
    }

    @JsonProperty("tag_list")
    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    @JsonProperty("owner")
    public Owner getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("name_with_namespace")
    public String getNameWithNamespace() {
        return nameWithNamespace;
    }

    @JsonProperty("name_with_namespace")
    public void setNameWithNamespace(String nameWithNamespace) {
        this.nameWithNamespace = nameWithNamespace;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("path_with_namespace")
    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    @JsonProperty("path_with_namespace")
    public void setPathWithNamespace(String pathWithNamespace) {
        this.pathWithNamespace = pathWithNamespace;
    }

    @JsonProperty("issues_enabled")
    public Boolean getIssuesEnabled() {
        return issuesEnabled;
    }

    @JsonProperty("issues_enabled")
    public void setIssuesEnabled(Boolean issuesEnabled) {
        this.issuesEnabled = issuesEnabled;
    }

    @JsonProperty("open_issues_count")
    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    @JsonProperty("open_issues_count")
    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    @JsonProperty("merge_requests_enabled")
    public Boolean getMergeRequestsEnabled() {
        return mergeRequestsEnabled;
    }

    @JsonProperty("merge_requests_enabled")
    public void setMergeRequestsEnabled(Boolean mergeRequestsEnabled) {
        this.mergeRequestsEnabled = mergeRequestsEnabled;
    }

    @JsonProperty("jobs_enabled")
    public Boolean getJobsEnabled() {
        return jobsEnabled;
    }

    @JsonProperty("jobs_enabled")
    public void setJobsEnabled(Boolean jobsEnabled) {
        this.jobsEnabled = jobsEnabled;
    }

    @JsonProperty("wiki_enabled")
    public Boolean getWikiEnabled() {
        return wikiEnabled;
    }

    @JsonProperty("wiki_enabled")
    public void setWikiEnabled(Boolean wikiEnabled) {
        this.wikiEnabled = wikiEnabled;
    }

    @JsonProperty("snippets_enabled")
    public Boolean getSnippetsEnabled() {
        return snippetsEnabled;
    }

    @JsonProperty("snippets_enabled")
    public void setSnippetsEnabled(Boolean snippetsEnabled) {
        this.snippetsEnabled = snippetsEnabled;
    }

    @JsonProperty("resolve_outdated_diff_discussions")
    public Boolean getResolveOutdatedDiffDiscussions() {
        return resolveOutdatedDiffDiscussions;
    }

    @JsonProperty("resolve_outdated_diff_discussions")
    public void setResolveOutdatedDiffDiscussions(Boolean resolveOutdatedDiffDiscussions) {
        this.resolveOutdatedDiffDiscussions = resolveOutdatedDiffDiscussions;
    }

    @JsonProperty("container_registry_enabled")
    public Boolean getContainerRegistryEnabled() {
        return containerRegistryEnabled;
    }

    @JsonProperty("container_registry_enabled")
    public void setContainerRegistryEnabled(Boolean containerRegistryEnabled) {
        this.containerRegistryEnabled = containerRegistryEnabled;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("last_activity_at")
    public String getLastActivityAt() {
        return lastActivityAt;
    }

    @JsonProperty("last_activity_at")
    public void setLastActivityAt(String lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    @JsonProperty("creator_id")
    public Integer getCreatorId() {
        return creatorId;
    }

    @JsonProperty("creator_id")
    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    @JsonProperty("namespace")
    public Namespace getNamespace() {
        return namespace;
    }

    @JsonProperty("namespace")
    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    @JsonProperty("import_status")
    public String getImportStatus() {
        return importStatus;
    }

    @JsonProperty("import_status")
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }

    @JsonProperty("import_error")
    public Object getImportError() {
        return importError;
    }

    @JsonProperty("import_error")
    public void setImportError(Object importError) {
        this.importError = importError;
    }

    @JsonProperty("permissions")
    public Permissions getPermissions() {
        return permissions;
    }

    @JsonProperty("permissions")
    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("archived")
    public Boolean getArchived() {
        return archived;
    }

    @JsonProperty("archived")
    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    @JsonProperty("avatar_url")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @JsonProperty("avatar_url")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @JsonProperty("license_url")
    public String getLicenseUrl() {
        return licenseUrl;
    }

    @JsonProperty("license_url")
    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    @JsonProperty("license")
    public License getLicense() {
        return license;
    }

    @JsonProperty("license")
    public void setLicense(License license) {
        this.license = license;
    }

    @JsonProperty("shared_runners_enabled")
    public Boolean getSharedRunnersEnabled() {
        return sharedRunnersEnabled;
    }

    @JsonProperty("shared_runners_enabled")
    public void setSharedRunnersEnabled(Boolean sharedRunnersEnabled) {
        this.sharedRunnersEnabled = sharedRunnersEnabled;
    }

    @JsonProperty("forks_count")
    public Integer getForksCount() {
        return forksCount;
    }

    @JsonProperty("forks_count")
    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
    }

    @JsonProperty("star_count")
    public Integer getStarCount() {
        return starCount;
    }

    @JsonProperty("star_count")
    public void setStarCount(Integer starCount) {
        this.starCount = starCount;
    }

    @JsonProperty("runners_token")
    public String getRunnersToken() {
        return runnersToken;
    }

    @JsonProperty("runners_token")
    public void setRunnersToken(String runnersToken) {
        this.runnersToken = runnersToken;
    }

    @JsonProperty("public_jobs")
    public Boolean getPublicJobs() {
        return publicJobs;
    }

    @JsonProperty("public_jobs")
    public void setPublicJobs(Boolean publicJobs) {
        this.publicJobs = publicJobs;
    }

    @JsonProperty("shared_with_groups")
    public List<SharedWithGroup> getSharedWithGroups() {
        return sharedWithGroups;
    }

    @JsonProperty("shared_with_groups")
    public void setSharedWithGroups(List<SharedWithGroup> sharedWithGroups) {
        this.sharedWithGroups = sharedWithGroups;
    }

    @JsonProperty("repository_storage")
    public String getRepositoryStorage() {
        return repositoryStorage;
    }

    @JsonProperty("repository_storage")
    public void setRepositoryStorage(String repositoryStorage) {
        this.repositoryStorage = repositoryStorage;
    }

    @JsonProperty("only_allow_merge_if_pipeline_succeeds")
    public Boolean getOnlyAllowMergeIfPipelineSucceeds() {
        return onlyAllowMergeIfPipelineSucceeds;
    }

    @JsonProperty("only_allow_merge_if_pipeline_succeeds")
    public void setOnlyAllowMergeIfPipelineSucceeds(Boolean onlyAllowMergeIfPipelineSucceeds) {
        this.onlyAllowMergeIfPipelineSucceeds = onlyAllowMergeIfPipelineSucceeds;
    }

    @JsonProperty("only_allow_merge_if_all_discussions_are_resolved")
    public Boolean getOnlyAllowMergeIfAllDiscussionsAreResolved() {
        return onlyAllowMergeIfAllDiscussionsAreResolved;
    }

    @JsonProperty("only_allow_merge_if_all_discussions_are_resolved")
    public void setOnlyAllowMergeIfAllDiscussionsAreResolved(Boolean onlyAllowMergeIfAllDiscussionsAreResolved) {
        this.onlyAllowMergeIfAllDiscussionsAreResolved = onlyAllowMergeIfAllDiscussionsAreResolved;
    }

    @JsonProperty("printing_merge_requests_link_enabled")
    public Boolean getPrintingMergeRequestsLinkEnabled() {
        return printingMergeRequestsLinkEnabled;
    }

    @JsonProperty("printing_merge_requests_link_enabled")
    public void setPrintingMergeRequestsLinkEnabled(Boolean printingMergeRequestsLinkEnabled) {
        this.printingMergeRequestsLinkEnabled = printingMergeRequestsLinkEnabled;
    }

    @JsonProperty("request_access_enabled")
    public Boolean getRequestAccessEnabled() {
        return requestAccessEnabled;
    }

    @JsonProperty("request_access_enabled")
    public void setRequestAccessEnabled(Boolean requestAccessEnabled) {
        this.requestAccessEnabled = requestAccessEnabled;
    }

    @JsonProperty("merge_method")
    public String getMergeMethod() {
        return mergeMethod;
    }

    @JsonProperty("merge_method")
    public void setMergeMethod(String mergeMethod) {
        this.mergeMethod = mergeMethod;
    }

    @JsonProperty("approvals_before_merge")
    public Integer getApprovalsBeforeMerge() {
        return approvalsBeforeMerge;
    }

    @JsonProperty("approvals_before_merge")
    public void setApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
    }

    @JsonProperty("statistics")
    public Statistics getStatistics() {
        return statistics;
    }

    @JsonProperty("statistics")
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
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
