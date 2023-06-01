package io.choerodon.devops.infra.dto.vuln;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/5/31 17:40
 */
public class VulnTargetDTO {
    @JsonProperty("Target")
    private String target;
    @JsonProperty("Vulnerabilities")
    private List<VulnerabilityDTO> vulnerabilities;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<VulnerabilityDTO> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<VulnerabilityDTO> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}
