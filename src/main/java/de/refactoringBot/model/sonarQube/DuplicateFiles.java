package de.refactoringBot.model.sonarQube;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

// TODO JsonProperty verbessern
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
})
public class DuplicateFiles {
    @JsonProperty
    private DuplicateFile fileInfo1;
    @JsonProperty
    private DuplicateFile fileInfo2;
    @JsonProperty
    private DuplicateFile fileInfo3;
    @JsonProperty
    private DuplicateFile fileInfo4;
    @JsonProperty
    private DuplicateFile fileInfo5;
    @JsonProperty
    private DuplicateFile fileInfo6;
    @JsonProperty
    private DuplicateFile fileInfo7;
    @JsonProperty
    private DuplicateFile fileInfo8;
    @JsonProperty
    private DuplicateFile fileInfo9;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    public DuplicateFile getFileInfo(int ref) {
        if (ref == 1) {
            return getFileInfo1();
        } else if (ref == 2) {
            return getFileInfo2();
        } else if (ref == 3) {
            return getFileInfo3();
        } else if (ref == 4) {
            return getFileInfo4();
        } else if (ref == 5) {
            return getFileInfo5();
        } else if (ref == 6) {
            return getFileInfo6();
        } else if (ref == 7) {
            return getFileInfo7();
        } else if (ref == 8) {
            return getFileInfo8();
        } else if (ref == 9) {
            return getFileInfo9();
        } else {
            return null;
        }
    }

    @JsonProperty("1")
    public DuplicateFile getFileInfo1() {
        return fileInfo1;
    }

    @JsonProperty("1")
    public void setFileInfo1(DuplicateFile fileInfo1) {
        this.fileInfo1 = fileInfo1;
    }

    @JsonProperty("2")
    public DuplicateFile getFileInfo2() {
        return fileInfo2;
    }

    @JsonProperty("2")
    public void setFileInfo2(DuplicateFile fileInfo2) {
        this.fileInfo2 = fileInfo2;
    }

    @JsonProperty("3")
    public DuplicateFile getFileInfo3() {
        return fileInfo3;
    }

    @JsonProperty("3")
    public void setFileInfo3(DuplicateFile fileInfo3) {
        this.fileInfo3 = fileInfo3;
    }

    @JsonProperty("4")
    public DuplicateFile getFileInfo4() {
        return fileInfo4;
    }

    @JsonProperty("4")
    public void setFileInfo4(DuplicateFile fileInfo4) {
        this.fileInfo4 = fileInfo4;
    }

    @JsonProperty("5")
    public DuplicateFile getFileInfo5() {
        return fileInfo5;
    }

    @JsonProperty("5")
    public void setFileInfo5(DuplicateFile fileInfo5) {
        this.fileInfo5 = fileInfo5;
    }

    @JsonProperty("6")
    public DuplicateFile getFileInfo6() {
        return fileInfo6;
    }

    @JsonProperty("6")
    public void setFileInfo6(DuplicateFile fileInfo6) {
        this.fileInfo6 = fileInfo6;
    }

    @JsonProperty("7")
    public DuplicateFile getFileInfo7() {
        return fileInfo7;
    }

    @JsonProperty("7")
    public void setFileInfo7(DuplicateFile fileInfo7) {
        this.fileInfo7 = fileInfo7;
    }

    @JsonProperty("8")
    public DuplicateFile getFileInfo8() {
        return fileInfo8;
    }

    @JsonProperty("8")
    public void setFileInfo8(DuplicateFile fileInfo8) {
        this.fileInfo8 = fileInfo8;
    }

    @JsonProperty("9")
    public DuplicateFile getFileInfo9() {
        return fileInfo9;
    }

    @JsonProperty("9")
    public void setFileInfo9(DuplicateFile fileInfo9) {
        this.fileInfo9 = fileInfo9;
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
