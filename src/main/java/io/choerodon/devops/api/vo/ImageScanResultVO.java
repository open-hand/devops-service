package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Created by wangxiang on 2021/3/25
 */
public class ImageScanResultVO {
    @JsonProperty("Target")
    private String target;
    @JsonProperty("Vulnerabilities")
    private List<VulnerabilitieVO> vulnerabilities;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<VulnerabilitieVO> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<VulnerabilitieVO> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}
