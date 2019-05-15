package de.refactoringBot.model.sonarQube;

import com.fasterxml.jackson.annotation.*;
import de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones.LiteralInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "from",
        "size",
        "_ref",
})
public class Block {
    @JsonProperty
    private Integer from;
    @JsonProperty
    private Integer size;
    @JsonProperty
    private Integer _ref;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    private long methodStartLine;
    private ArrayList<LiteralInfo> literals;
    private String filePath;


    @JsonProperty("from")
    public Integer getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(Integer from) {
        this.from = from;
    }

    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

    @JsonProperty("_ref")
    public Integer getRef() {
        return _ref;
    }

    @JsonProperty("_ref")
    public void setRef(Integer _ref) {
        this._ref = _ref;
    }

    public long getMethodStartLine() {
        return methodStartLine;
    }

    public void setMethodStartLine(long methodStartLine) {
        this.methodStartLine = methodStartLine;
    }


    public ArrayList<LiteralInfo> getLiterals() {
        return literals;
    }

    public void setLiterals(ArrayList<LiteralInfo> literals) {
        this.literals = literals;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
