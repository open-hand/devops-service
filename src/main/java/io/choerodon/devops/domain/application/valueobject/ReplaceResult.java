package io.choerodon.devops.domain.application.valueobject;

import java.util.List;
import java.util.Map;

/**
 * @author crockitwood
 */
public class ReplaceResult {
    private String yaml;
    private List<HighlightMarker> highlightMarkers;
    private Integer totalLine;
    private String errorMsg;
    private Map<Long,String> errorLines;

    public String getYaml() {
        return yaml;
    }

    public void setYaml(String yaml) {
        this.yaml = yaml;
    }

    public List<HighlightMarker> getHighlightMarkers() {
        return highlightMarkers;
    }

    public void setHighlightMarkers(List<HighlightMarker> highlightMarkers) {
        this.highlightMarkers = highlightMarkers;
    }

    public Integer getTotalLine() {
        return totalLine;
    }

    public void setTotalLine(Integer totalLine) {
        this.totalLine = totalLine;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Map<Long, String> getErrorLines() {
        return errorLines;
    }

    public void setErrorLines(Map<Long, String> errorLines) {
        this.errorLines = errorLines;
    }
}
