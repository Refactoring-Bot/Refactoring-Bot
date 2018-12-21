
package de.refactoringBot.model.sonarQube;

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
@JsonPropertyOrder({ "total", "p", "ps", "paging", "effortTotal", "debtTotal", "issues", "components", "facets" })
public class SonarQubeIssues {

	@JsonProperty("total")
	private Integer total;
	@JsonProperty("p")
	private Integer p;
	@JsonProperty("ps")
	private Integer ps;
	@JsonProperty("paging")
	private Paging paging;
	@JsonProperty("effortTotal")
	private Integer effortTotal;
	@JsonProperty("debtTotal")
	private Integer debtTotal;
	@JsonProperty("issues")
	private List<SonarIssue> issues = null;
	@JsonProperty("components")
	private List<Component> components = null;
	@JsonProperty("facets")
	private List<Object> facets = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("total")
	public Integer getTotal() {
		return total;
	}

	@JsonProperty("total")
	public void setTotal(Integer total) {
		this.total = total;
	}

	@JsonProperty("p")
	public Integer getP() {
		return p;
	}

	@JsonProperty("p")
	public void setP(Integer p) {
		this.p = p;
	}

	@JsonProperty("ps")
	public Integer getPs() {
		return ps;
	}

	@JsonProperty("ps")
	public void setPs(Integer ps) {
		this.ps = ps;
	}

	@JsonProperty("paging")
	public Paging getPaging() {
		return paging;
	}

	@JsonProperty("paging")
	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	@JsonProperty("effortTotal")
	public Integer getEffortTotal() {
		return effortTotal;
	}

	@JsonProperty("effortTotal")
	public void setEffortTotal(Integer effortTotal) {
		this.effortTotal = effortTotal;
	}

	@JsonProperty("debtTotal")
	public Integer getDebtTotal() {
		return debtTotal;
	}

	@JsonProperty("debtTotal")
	public void setDebtTotal(Integer debtTotal) {
		this.debtTotal = debtTotal;
	}

	@JsonProperty("issues")
	public List<SonarIssue> getIssues() {
		return issues;
	}

	@JsonProperty("issues")
	public void setIssues(List<SonarIssue> issues) {
		this.issues = issues;
	}

	@JsonProperty("components")
	public List<Component> getComponents() {
		return components;
	}

	@JsonProperty("components")
	public void setComponents(List<Component> components) {
		this.components = components;
	}

	@JsonProperty("facets")
	public List<Object> getFacets() {
		return facets;
	}

	@JsonProperty("facets")
	public void setFacets(List<Object> facets) {
		this.facets = facets;
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
