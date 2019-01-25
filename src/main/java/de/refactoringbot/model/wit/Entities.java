
package de.refactoringbot.model.wit;

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
    "refactoringString"
})
public class Entities {

    @JsonProperty("refactoringObject")
    private List<RefactoringObject> refactoringObject = null;
    @JsonProperty("refactoringOperation")
    private List<RefactoringOperation> refactoringOperation = null;
    @JsonProperty("refactoringString")
    private List<RefactoringString> refactoringString = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("refactoringObject")
    public List<RefactoringObject> getRefactoringObject() {
        return refactoringObject;
    }

    @JsonProperty("refactoringObject")
    public void setRefactoringObject(List<RefactoringObject> refactoringObject) {
        this.refactoringObject = refactoringObject;
    }

    @JsonProperty("refactoringOperation")
    public List<RefactoringOperation> getRefactoringOperation() {
        return refactoringOperation;
    }

    @JsonProperty("refactoringOperation")
    public void setRefactoringOperation(List<RefactoringOperation> refactoringOperation) {
        this.refactoringOperation = refactoringOperation;
    }

    @JsonProperty("refactoringString")
    public List<RefactoringString> getRefactoringString() {
        return refactoringString;
    }

    @JsonProperty("refactoringString")
    public void setRefactoringString(List<RefactoringString> refactoringString) {
        this.refactoringString = refactoringString;
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
