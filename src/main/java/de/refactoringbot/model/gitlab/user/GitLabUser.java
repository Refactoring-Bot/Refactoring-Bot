
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
    "id",
    "username",
    "name",
    "state",
    "avatar_url",
    "web_url",
    "created_at",
    "bio",
    "location",
    "public_email",
    "skype",
    "linkedin",
    "twitter",
    "website_url",
    "organization"
})
public class GitLabUser {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("name")
    private String name;
    @JsonProperty("state")
    private String state;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("web_url")
    private String webUrl;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("bio")
    private Object bio;
    @JsonProperty("location")
    private Object location;
    @JsonProperty("public_email")
    private String publicEmail;
    @JsonProperty("skype")
    private String skype;
    @JsonProperty("linkedin")
    private String linkedin;
    @JsonProperty("twitter")
    private String twitter;
    @JsonProperty("website_url")
    private String websiteUrl;
    @JsonProperty("organization")
    private String organization;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("avatar_url")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @JsonProperty("avatar_url")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @JsonProperty("web_url")
    public String getWebUrl() {
        return webUrl;
    }

    @JsonProperty("web_url")
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("bio")
    public Object getBio() {
        return bio;
    }

    @JsonProperty("bio")
    public void setBio(Object bio) {
        this.bio = bio;
    }

    @JsonProperty("location")
    public Object getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(Object location) {
        this.location = location;
    }

    @JsonProperty("public_email")
    public String getPublicEmail() {
        return publicEmail;
    }

    @JsonProperty("public_email")
    public void setPublicEmail(String publicEmail) {
        this.publicEmail = publicEmail;
    }

    @JsonProperty("skype")
    public String getSkype() {
        return skype;
    }

    @JsonProperty("skype")
    public void setSkype(String skype) {
        this.skype = skype;
    }

    @JsonProperty("linkedin")
    public String getLinkedin() {
        return linkedin;
    }

    @JsonProperty("linkedin")
    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    @JsonProperty("twitter")
    public String getTwitter() {
        return twitter;
    }

    @JsonProperty("twitter")
    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    @JsonProperty("website_url")
    public String getWebsiteUrl() {
        return websiteUrl;
    }

    @JsonProperty("website_url")
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    @JsonProperty("organization")
    public String getOrganization() {
        return organization;
    }

    @JsonProperty("organization")
    public void setOrganization(String organization) {
        this.organization = organization;
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
