
package de.refactoringbot.model.sonarqube;

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
@JsonPropertyOrder({ "key", "rule", "severity", "component", "project", "line", "hash", "textRange", "flows", "status",
		"message", "effort", "debt", "assignee", "tags", "creationDate", "updateDate", "type", "organization",
		"fromHotspot", "resolution", "closeDate" })
public class SonarIssue {

	@JsonProperty("key")
	private String key;
	@JsonProperty("rule")
	private String rule;
	@JsonProperty("severity")
	private String severity;
	@JsonProperty("component")
	private String component;
	@JsonProperty("project")
	private String project;
	@JsonProperty("line")
	private Integer line;
	@JsonProperty("hash")
	private String hash;
	@JsonProperty("textRange")
	private TextRange textRange;
	@JsonProperty("flows")
	private List<Flow> flows = null;
	@JsonProperty("status")
	private String status;
	@JsonProperty("message")
	private String message;
	@JsonProperty("effort")
	private String effort;
	@JsonProperty("debt")
	private String debt;
	@JsonProperty("assignee")
	private String assignee;
	@JsonProperty("tags")
	private List<String> tags = null;
	@JsonProperty("creationDate")
	private String creationDate;
	@JsonProperty("updateDate")
	private String updateDate;
	@JsonProperty("type")
	private String type;
	@JsonProperty("organization")
	private String organization;
	@JsonProperty("fromHotspot")
	private Boolean fromHotspot;
	@JsonProperty("resolution")
	private String resolution;
	@JsonProperty("closeDate")
	private String closeDate;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@JsonProperty("key")
	public String getKey() {
		return key;
	}

	@JsonProperty("key")
	public void setKey(String key) {
		this.key = key;
	}

	@JsonProperty("rule")
	public String getRule() {
		return rule;
	}

	@JsonProperty("rule")
	public void setRule(String rule) {
		this.rule = rule;
	}

	@JsonProperty("severity")
	public String getSeverity() {
		return severity;
	}

	@JsonProperty("severity")
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	@JsonProperty("component")
	public String getComponent() {
		return component;
	}

	@JsonProperty("component")
	public void setComponent(String component) {
		this.component = component;
	}

	@JsonProperty("project")
	public String getProject() {
		return project;
	}

	@JsonProperty("project")
	public void setProject(String project) {
		this.project = project;
	}

	@JsonProperty("line")
	public Integer getLine() {
		return line;
	}

	@JsonProperty("line")
	public void setLine(Integer line) {
		this.line = line;
	}

	@JsonProperty("hash")
	public String getHash() {
		return hash;
	}

	@JsonProperty("hash")
	public void setHash(String hash) {
		this.hash = hash;
	}

	@JsonProperty("textRange")
	public TextRange getTextRange() {
		return textRange;
	}

	@JsonProperty("textRange")
	public void setTextRange(TextRange textRange) {
		this.textRange = textRange;
	}

	@JsonProperty("flows")
	public List<Flow> getFlows() {
		return flows;
	}

	@JsonProperty("flows")
	public void setFlows(List<Flow> flows) {
		this.flows = flows;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	@JsonProperty("message")
	public void setMessage(String message) {
		this.message = message;
	}

	@JsonProperty("effort")
	public String getEffort() {
		return effort;
	}

	@JsonProperty("effort")
	public void setEffort(String effort) {
		this.effort = effort;
	}

	@JsonProperty("debt")
	public String getDebt() {
		return debt;
	}

	@JsonProperty("debt")
	public void setDebt(String debt) {
		this.debt = debt;
	}

	@JsonProperty("assignee")
	public String getAssignee() {
		return assignee;
	}

	@JsonProperty("assignee")
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	@JsonProperty("tags")
	public List<String> getTags() {
		return tags;
	}

	@JsonProperty("tags")
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	@JsonProperty("creationDate")
	public String getCreationDate() {
		return creationDate;
	}

	@JsonProperty("creationDate")
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	@JsonProperty("updateDate")
	public String getUpdateDate() {
		return updateDate;
	}

	@JsonProperty("updateDate")
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	@JsonProperty("type")
	public String getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("organization")
	public String getOrganization() {
		return organization;
	}

	@JsonProperty("organization")
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@JsonProperty("fromHotspot")
	public Boolean getFromHotspot() {
		return fromHotspot;
	}

	@JsonProperty("fromHotspot")
	public void setFromHotspot(Boolean fromHotspot) {
		this.fromHotspot = fromHotspot;
	}

	@JsonProperty("resolution")
	public String getResolution() {
		return resolution;
	}

	@JsonProperty("resolution")
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	@JsonProperty("closeDate")
	public String getCloseDate() {
		return closeDate;
	}

	@JsonProperty("closeDate")
	public void setCloseDate(String closeDate) {
		this.closeDate = closeDate;
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
