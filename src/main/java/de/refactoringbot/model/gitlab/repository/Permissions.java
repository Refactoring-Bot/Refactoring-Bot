
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
    "project_access",
    "group_access"
})
public class Permissions {

    @JsonProperty("project_access")
    private ProjectAccess projectAccess;
    @JsonProperty("group_access")
    private GroupAccess groupAccess;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("project_access")
    public ProjectAccess getProjectAccess() {
        return projectAccess;
    }

    @JsonProperty("project_access")
    public void setProjectAccess(ProjectAccess projectAccess) {
        this.projectAccess = projectAccess;
    }

    @JsonProperty("group_access")
    public GroupAccess getGroupAccess() {
        return groupAccess;
    }

    @JsonProperty("group_access")
    public void setGroupAccess(GroupAccess groupAccess) {
        this.groupAccess = groupAccess;
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
