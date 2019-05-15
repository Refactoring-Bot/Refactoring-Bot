package de.refactoringBot.model.sonarQube;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "duplications",
        "files",
})
public class Duplicates {
    @JsonProperty
    private List<Blocks> duplications;
    @JsonProperty
    private DuplicateFiles files;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    @JsonProperty("duplications")
    public List<Blocks> getDuplications() {
        return duplications;
    }

    @JsonProperty("duplications")
    public void setDuplications(List<Blocks> duplications) {
        this.duplications = duplications;
    }

    @JsonProperty("files")
    public DuplicateFiles getDuplicateFiles() {
        return files;
    }

    @JsonProperty("files")
    public void setDuplicateFiles(DuplicateFiles files) {
        this.files = files;
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
