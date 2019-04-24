
package de.refactoringbot.model.gitlab.user;

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
    "id",
    "name",
    "username",
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
    "organization",
    "last_sign_in_at",
    "confirmed_at",
    "last_activity_on",
    "email",
    "theme_id",
    "color_scheme_id",
    "projects_limit",
    "current_sign_in_at",
    "identities",
    "can_create_group",
    "can_create_project",
    "two_factor_enabled",
    "external",
    "private_profile",
    "shared_runners_minutes_limit"
})
public class GitLabUser {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("username")
    private String username;
    @JsonProperty("state")
    private String state;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("web_url")
    private String webUrl;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("bio")
    private String bio;
    @JsonProperty("location")
    private String location;
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
    @JsonProperty("last_sign_in_at")
    private String lastSignInAt;
    @JsonProperty("confirmed_at")
    private String confirmedAt;
    @JsonProperty("last_activity_on")
    private String lastActivityOn;
    @JsonProperty("email")
    private String email;
    @JsonProperty("theme_id")
    private Integer themeId;
    @JsonProperty("color_scheme_id")
    private Integer colorSchemeId;
    @JsonProperty("projects_limit")
    private Integer projectsLimit;
    @JsonProperty("current_sign_in_at")
    private String currentSignInAt;
    @JsonProperty("identities")
    private List<Identity> identities = null;
    @JsonProperty("can_create_group")
    private Boolean canCreateGroup;
    @JsonProperty("can_create_project")
    private Boolean canCreateProject;
    @JsonProperty("two_factor_enabled")
    private Boolean twoFactorEnabled;
    @JsonProperty("external")
    private Boolean external;
    @JsonProperty("private_profile")
    private Boolean privateProfile;
    @JsonProperty("shared_runners_minutes_limit")
    private Object sharedRunnersMinutesLimit;
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

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
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
    public String getBio() {
        return bio;
    }

    @JsonProperty("bio")
    public void setBio(String bio) {
        this.bio = bio;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(String location) {
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

    @JsonProperty("last_sign_in_at")
    public String getLastSignInAt() {
        return lastSignInAt;
    }

    @JsonProperty("last_sign_in_at")
    public void setLastSignInAt(String lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }

    @JsonProperty("confirmed_at")
    public String getConfirmedAt() {
        return confirmedAt;
    }

    @JsonProperty("confirmed_at")
    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    @JsonProperty("last_activity_on")
    public String getLastActivityOn() {
        return lastActivityOn;
    }

    @JsonProperty("last_activity_on")
    public void setLastActivityOn(String lastActivityOn) {
        this.lastActivityOn = lastActivityOn;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("theme_id")
    public Integer getThemeId() {
        return themeId;
    }

    @JsonProperty("theme_id")
    public void setThemeId(Integer themeId) {
        this.themeId = themeId;
    }

    @JsonProperty("color_scheme_id")
    public Integer getColorSchemeId() {
        return colorSchemeId;
    }

    @JsonProperty("color_scheme_id")
    public void setColorSchemeId(Integer colorSchemeId) {
        this.colorSchemeId = colorSchemeId;
    }

    @JsonProperty("projects_limit")
    public Integer getProjectsLimit() {
        return projectsLimit;
    }

    @JsonProperty("projects_limit")
    public void setProjectsLimit(Integer projectsLimit) {
        this.projectsLimit = projectsLimit;
    }

    @JsonProperty("current_sign_in_at")
    public String getCurrentSignInAt() {
        return currentSignInAt;
    }

    @JsonProperty("current_sign_in_at")
    public void setCurrentSignInAt(String currentSignInAt) {
        this.currentSignInAt = currentSignInAt;
    }

    @JsonProperty("identities")
    public List<Identity> getIdentities() {
        return identities;
    }

    @JsonProperty("identities")
    public void setIdentities(List<Identity> identities) {
        this.identities = identities;
    }

    @JsonProperty("can_create_group")
    public Boolean getCanCreateGroup() {
        return canCreateGroup;
    }

    @JsonProperty("can_create_group")
    public void setCanCreateGroup(Boolean canCreateGroup) {
        this.canCreateGroup = canCreateGroup;
    }

    @JsonProperty("can_create_project")
    public Boolean getCanCreateProject() {
        return canCreateProject;
    }

    @JsonProperty("can_create_project")
    public void setCanCreateProject(Boolean canCreateProject) {
        this.canCreateProject = canCreateProject;
    }

    @JsonProperty("two_factor_enabled")
    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    @JsonProperty("two_factor_enabled")
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    @JsonProperty("external")
    public Boolean getExternal() {
        return external;
    }

    @JsonProperty("external")
    public void setExternal(Boolean external) {
        this.external = external;
    }

    @JsonProperty("private_profile")
    public Boolean getPrivateProfile() {
        return privateProfile;
    }

    @JsonProperty("private_profile")
    public void setPrivateProfile(Boolean privateProfile) {
        this.privateProfile = privateProfile;
    }

    @JsonProperty("shared_runners_minutes_limit")
    public Object getSharedRunnersMinutesLimit() {
        return sharedRunnersMinutesLimit;
    }

    @JsonProperty("shared_runners_minutes_limit")
    public void setSharedRunnersMinutesLimit(Object sharedRunnersMinutesLimit) {
        this.sharedRunnersMinutesLimit = sharedRunnersMinutesLimit;
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
