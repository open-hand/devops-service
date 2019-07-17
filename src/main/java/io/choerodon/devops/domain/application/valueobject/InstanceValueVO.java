package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

import io.choerodon.devops.api.vo.ErrorLineVO;

/**
 * yaml 转换结果
 *
 * @author crockitwood
 */
public class InstanceValueVO {
    private String yaml;
    private List<HighlightMarker> highlightMarkers;
    private Integer totalLine;
    private String errorMsg;
    private List<ErrorLineVO> errorLines;
    private List<Integer> newLines;
    private String deltaYaml;
    private String name;
    private Long  id;
    private Long objectVersionNumber;

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

    public List<ErrorLineVO> getErrorLines() {
        return errorLines;
    }

    public void setErrorLines(List<ErrorLineVO> errorLines) {
        this.errorLines = errorLines;
    }

    public List<Integer> getNewLines() {
        return newLines;
    }

    public void setNewLines(List<Integer> newLines) {
        this.newLines = newLines;
    }

    public String getDeltaYaml() {
        return deltaYaml;
    }

    public void setDeltaYaml(String deltaYaml) {
        this.deltaYaml = deltaYaml;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
