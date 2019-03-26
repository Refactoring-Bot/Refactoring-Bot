
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
    "access_level",
    "notification_level"
})
public class ProjectAccess {

    @JsonProperty("access_level")
    private Integer accessLevel;
    @JsonProperty("notification_level")
    private Integer notificationLevel;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("access_level")
    public Integer getAccessLevel() {
        return accessLevel;
    }

    @JsonProperty("access_level")
    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    @JsonProperty("notification_level")
    public Integer getNotificationLevel() {
        return notificationLevel;
    }

    @JsonProperty("notification_level")
    public void setNotificationLevel(Integer notificationLevel) {
        this.notificationLevel = notificationLevel;
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
