package io.choerodon.devops.api.vo.vuln;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/5/31 17:40
 */
public class VulnTargetVO {
    @JsonProperty("Target")
    private String target;
    @JsonProperty("Vulnerabilities")
    private List<VulnerabilityVO> vulnerabilities;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<VulnerabilityVO> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<VulnerabilityVO> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}
