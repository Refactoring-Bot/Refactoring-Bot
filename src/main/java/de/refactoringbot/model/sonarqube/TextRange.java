
package de.refactoringbot.model.sonarqube;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "startLine", "endLine", "startOffset", "endOffset" })
public class TextRange {

	@JsonProperty("startLine")
	private Integer startLine;
	@JsonProperty("endLine")
	private Integer endLine;
	@JsonProperty("startOffset")
	private Integer startOffset;
	@JsonProperty("endOffset")
	private Integer endOffset;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@JsonProperty("startLine")
	public Integer getStartLine() {
		return startLine;
	}

	@JsonProperty("startLine")
	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}

	@JsonProperty("endLine")
	public Integer getEndLine() {
		return endLine;
	}

	@JsonProperty("endLine")
	public void setEndLine(Integer endLine) {
		this.endLine = endLine;
	}

	@JsonProperty("startOffset")
	public Integer getStartOffset() {
		return startOffset;
	}

	@JsonProperty("startOffset")
	public void setStartOffset(Integer startOffset) {
		this.startOffset = startOffset;
	}

	@JsonProperty("endOffset")
	public Integer getEndOffset() {
		return endOffset;
	}

	@JsonProperty("endOffset")
	public void setEndOffset(Integer endOffset) {
		this.endOffset = endOffset;
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
