package io.choerodon.devops.api.vo.vuln;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/5/31 17:40
 */
public class VulnTargetVO {
    private String target;
    private List<VulnerabilityVO> vulnerabilities;

    public VulnTargetVO() {
    }

    public VulnTargetVO(String target, List<VulnerabilityVO> vulnerabilities) {
        this.target = target;
        this.vulnerabilities = vulnerabilities;
    }

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
