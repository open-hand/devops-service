package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Created by Sheep on 2019/5/7.
 */

public class SonarTableVO {

    private List<String> dates;
    private List<String> bugs;
    private List<String> codeSmells;
    private List<String> vulnerabilities;
    private List<String> linesToCover;
    private List<String> coverLines;
    private List<String> coverage;
    private List<String> nclocs;
    private List<String> duplicatedLines;
    private List<String> duplicatedLinesRate;

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<String> getBugs() {
        return bugs;
    }

    public void setBugs(List<String> bugs) {
        this.bugs = bugs;
    }

    public List<String> getCodeSmells() {
        return codeSmells;
    }

    public void setCodeSmells(List<String> codeSmells) {
        this.codeSmells = codeSmells;
    }

    public List<String> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<String> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public List<String> getLinesToCover() {
        return linesToCover;
    }

    public void setLinesToCover(List<String> linesToCover) {
        this.linesToCover = linesToCover;
    }

    public List<String> getCoverLines() {
        return coverLines;
    }

    public void setCoverLines(List<String> coverLines) {
        this.coverLines = coverLines;
    }

    public List<String> getCoverage() {
        return coverage;
    }

    public void setCoverage(List<String> coverage) {
        this.coverage = coverage;
    }

    public List<String> getNclocs() {
        return nclocs;
    }

    public void setNclocs(List<String> nclocs) {
        this.nclocs = nclocs;
    }

    public List<String> getDuplicatedLines() {
        return duplicatedLines;
    }

    public void setDuplicatedLines(List<String> duplicatedLines) {
        this.duplicatedLines = duplicatedLines;
    }

    public List<String> getDuplicatedLinesRate() {
        return duplicatedLinesRate;
    }

    public void setDuplicatedLinesRate(List<String> duplicatedLinesRate) {
        this.duplicatedLinesRate = duplicatedLinesRate;
    }
}
