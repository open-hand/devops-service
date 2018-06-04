package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

/**
 * @author crockitwood
 */
public class ReplaceResult {
    private String yaml;
    private List<HighlightMarker> highlightMarkers;
    private Integer totalLine;

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
}
