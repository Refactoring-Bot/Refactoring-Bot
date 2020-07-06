
package de.refactoringbot.model.gitlab.pullrequest;

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
    "base_sha",
    "head_sha",
    "start_sha"
})
public class DiffRefs {

    @JsonProperty("base_sha")
    private String baseSha;
    @JsonProperty("head_sha")
    private String headSha;
    @JsonProperty("start_sha")
    private String startSha;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("base_sha")
    public String getBaseSha() {
        return baseSha;
    }

    @JsonProperty("base_sha")
    public void setBaseSha(String baseSha) {
        this.baseSha = baseSha;
    }

    @JsonProperty("head_sha")
    public String getHeadSha() {
        return headSha;
    }

    @JsonProperty("head_sha")
    public void setHeadSha(String headSha) {
        this.headSha = headSha;
    }

    @JsonProperty("start_sha")
    public String getStartSha() {
        return startSha;
    }

    @JsonProperty("start_sha")
    public void setStartSha(String startSha) {
        this.startSha = startSha;
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
