
package de.refactoringbot.model.wit;

import java.util.ArrayList;
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
    "refactoringObject",
    "refactoringOperation",
    "refactoringString",
    "javaAnnotations"
})
public class Entities {

    @JsonProperty("refactoringObject")
    private List<WitEntity> refactoringObject = new ArrayList<WitEntity>();
    @JsonProperty("refactoringOperation")
    private List<WitEntity> refactoringOperation = new ArrayList<WitEntity>();
    @JsonProperty("refactoringString")
    private List<WitEntity> refactoringString = new ArrayList<WitEntity>();
    @JsonProperty("javaAnnotations")
    private List<WitEntity> javaAnnotations = new ArrayList<WitEntity>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("refactoringObject")
    public List<WitEntity> getRefactoringObject() {
        return refactoringObject;
    }

    @JsonProperty("refactoringObject")
    public void setRefactoringObject(List<WitEntity> refactoringObject) {
        this.refactoringObject = refactoringObject;
    }

    @JsonProperty("refactoringOperation")
    public List<WitEntity> getRefactoringOperation() {
        return refactoringOperation;
    }

    @JsonProperty("refactoringOperation")
    public void setRefactoringOperation(List<WitEntity> refactoringOperation) {
        this.refactoringOperation = refactoringOperation;
    }

    @JsonProperty("refactoringString")
    public List<WitEntity> getRefactoringString() {
        return refactoringString;
    }

    @JsonProperty("refactoringString")
    public void setRefactoringString(List<WitEntity> refactoringString) {
        this.refactoringString = refactoringString;
    }
    
    @JsonProperty("javaAnnotations")
    public List<WitEntity> getJavaAnnotations() {
    return javaAnnotations;
    }

    @JsonProperty("javaAnnotations")
    public void setJavaAnnotations(List<WitEntity> javaAnnotations) {
    this.javaAnnotations = javaAnnotations;
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
