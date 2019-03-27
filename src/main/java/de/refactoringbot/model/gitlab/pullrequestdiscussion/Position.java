
package de.refactoringbot.model.gitlab.pullrequestdiscussion;

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
    "start_sha",
    "head_sha",
    "old_path",
    "new_path",
    "position_type",
    "old_line",
    "new_line"
})
public class Position {

    @JsonProperty("base_sha")
    private String baseSha;
    @JsonProperty("start_sha")
    private String startSha;
    @JsonProperty("head_sha")
    private String headSha;
    @JsonProperty("old_path")
    private String oldPath;
    @JsonProperty("new_path")
    private String newPath;
    @JsonProperty("position_type")
    private String positionType;
    @JsonProperty("old_line")
    private Integer oldLine;
    @JsonProperty("new_line")
    private Integer newLine;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("base_sha")
    public String getBaseSha() {
        return baseSha;
    }

    @JsonProperty("base_sha")
    public void setBaseSha(String baseSha) {
        this.baseSha = baseSha;
    }

    @JsonProperty("start_sha")
    public String getStartSha() {
        return startSha;
    }

    @JsonProperty("start_sha")
    public void setStartSha(String startSha) {
        this.startSha = startSha;
    }

    @JsonProperty("head_sha")
    public String getHeadSha() {
        return headSha;
    }

    @JsonProperty("head_sha")
    public void setHeadSha(String headSha) {
        this.headSha = headSha;
    }

    @JsonProperty("old_path")
    public String getOldPath() {
        return oldPath;
    }

    @JsonProperty("old_path")
    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    @JsonProperty("new_path")
    public String getNewPath() {
        return newPath;
    }

    @JsonProperty("new_path")
    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    @JsonProperty("position_type")
    public String getPositionType() {
        return positionType;
    }

    @JsonProperty("position_type")
    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    @JsonProperty("old_line")
    public Integer getOldLine() {
        return oldLine;
    }

    @JsonProperty("old_line")
    public void setOldLine(Integer oldLine) {
        this.oldLine = oldLine;
    }

    @JsonProperty("new_line")
    public Integer getNewLine() {
        return newLine;
    }

    @JsonProperty("new_line")
    public void setNewLine(Integer newLine) {
        this.newLine = newLine;
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
