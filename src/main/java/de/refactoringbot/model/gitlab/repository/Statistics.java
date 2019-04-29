
package de.refactoringbot.model.gitlab.repository;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "commit_count",
    "storage_size",
    "repository_size",
    "lfs_objects_size",
    "job_artifacts_size"
})
public class Statistics {

    @JsonProperty("commit_count")
    private Integer commitCount;
    @JsonProperty("storage_size")
    private Integer storageSize;
    @JsonProperty("repository_size")
    private Integer repositorySize;
    @JsonProperty("lfs_objects_size")
    private Integer lfsObjectsSize;
    @JsonProperty("job_artifacts_size")
    private Integer jobArtifactsSize;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("commit_count")
    public Integer getCommitCount() {
        return commitCount;
    }

    @JsonProperty("commit_count")
    public void setCommitCount(Integer commitCount) {
        this.commitCount = commitCount;
    }

    @JsonProperty("storage_size")
    public Integer getStorageSize() {
        return storageSize;
    }

    @JsonProperty("storage_size")
    public void setStorageSize(Integer storageSize) {
        this.storageSize = storageSize;
    }

    @JsonProperty("repository_size")
    public Integer getRepositorySize() {
        return repositorySize;
    }

    @JsonProperty("repository_size")
    public void setRepositorySize(Integer repositorySize) {
        this.repositorySize = repositorySize;
    }

    @JsonProperty("lfs_objects_size")
    public Integer getLfsObjectsSize() {
        return lfsObjectsSize;
    }

    @JsonProperty("lfs_objects_size")
    public void setLfsObjectsSize(Integer lfsObjectsSize) {
        this.lfsObjectsSize = lfsObjectsSize;
    }

    @JsonProperty("job_artifacts_size")
    public Integer getJobArtifactsSize() {
        return jobArtifactsSize;
    }

    @JsonProperty("job_artifacts_size")
    public void setJobArtifactsSize(Integer jobArtifactsSize) {
        this.jobArtifactsSize = jobArtifactsSize;
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
