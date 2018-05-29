package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

/**
 * @author crockitwood
 */
public class ReplaceResult {
    private String yaml;
    private List<HighlightMarker> highlightMarkers;

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
}
