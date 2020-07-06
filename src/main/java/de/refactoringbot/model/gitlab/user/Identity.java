
package de.refactoringbot.model.gitlab.user;

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
    "provider",
    "extern_uid"
})
public class Identity {

    @JsonProperty("provider")
    private String provider;
    @JsonProperty("extern_uid")
    private String externUid;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("provider")
    public String getProvider() {
        return provider;
    }

    @JsonProperty("provider")
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @JsonProperty("extern_uid")
    public String getExternUid() {
        return externUid;
    }

    @JsonProperty("extern_uid")
    public void setExternUid(String externUid) {
        this.externUid = externUid;
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
