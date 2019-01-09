
package de.refactoringbot.model.githubModels.user;

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
    "name",
    "space",
    "private_repos",
    "collaborators"
})
public class Plan {

    @JsonProperty("name")
    private String name;
    @JsonProperty("space")
    private Integer space;
    @JsonProperty("private_repos")
    private Integer privateRepos;
    @JsonProperty("collaborators")
    private Integer collaborators;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("space")
    public Integer getSpace() {
        return space;
    }

    @JsonProperty("space")
    public void setSpace(Integer space) {
        this.space = space;
    }

    @JsonProperty("private_repos")
    public Integer getPrivateRepos() {
        return privateRepos;
    }

    @JsonProperty("private_repos")
    public void setPrivateRepos(Integer privateRepos) {
        this.privateRepos = privateRepos;
    }

    @JsonProperty("collaborators")
    public Integer getCollaborators() {
        return collaborators;
    }

    @JsonProperty("collaborators")
    public void setCollaborators(Integer collaborators) {
        this.collaborators = collaborators;
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
