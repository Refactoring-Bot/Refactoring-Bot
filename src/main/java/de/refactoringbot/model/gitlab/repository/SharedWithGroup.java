
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
    "group_id",
    "group_name",
    "group_full_path",
    "group_access_level"
})
public class SharedWithGroup {

    @JsonProperty("group_id")
    private Integer groupId;
    @JsonProperty("group_name")
    private String groupName;
    @JsonProperty("group_full_path")
    private String groupFullPath;
    @JsonProperty("group_access_level")
    private Integer groupAccessLevel;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("group_id")
    public Integer getGroupId() {
        return groupId;
    }

    @JsonProperty("group_id")
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    @JsonProperty("group_name")
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty("group_name")
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @JsonProperty("group_full_path")
    public String getGroupFullPath() {
        return groupFullPath;
    }

    @JsonProperty("group_full_path")
    public void setGroupFullPath(String groupFullPath) {
        this.groupFullPath = groupFullPath;
    }

    @JsonProperty("group_access_level")
    public Integer getGroupAccessLevel() {
        return groupAccessLevel;
    }

    @JsonProperty("group_access_level")
    public void setGroupAccessLevel(Integer groupAccessLevel) {
        this.groupAccessLevel = groupAccessLevel;
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
