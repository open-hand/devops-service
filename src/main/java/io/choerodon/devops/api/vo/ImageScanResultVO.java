package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;

/**
 * Created by wangxiang on 2021/3/25
 */
public class ImageScanResultVO {
    @JsonProperty("Target")
    private String target;
    @JsonProperty("Vulnerabilities")
    private List<VulnerabilitieVO> vulnerabilities;

    /**
     * 镜像扫描开始时间
     */
    private Date startDate;
    /**
     * 耗时
     */
    private Long spendTime;

    /**
     * 未知漏洞个数
     */
    private Integer unknownCount;

    /**
     * 低危漏洞个数
     */
    private Integer lowCount;

    /**
     * 中危漏洞个数
     */
    private Integer mediumCount;

    /**
     * 高危漏洞个数
     */
    private Integer highCount;


    /**
     * 危急漏洞个数
     */
    private Integer criticalCount;

    /**
     * 漏洞严重程度
     * @link{ImageSecurityEnum}
     */
    private String level;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Long getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(Long spendTime) {
        this.spendTime = spendTime;
    }

    public Integer getUnknownCount() {
        return unknownCount;
    }

    public void setUnknownCount(Integer unknownCount) {
        this.unknownCount = unknownCount;
    }

    public Integer getLowCount() {
        return lowCount;
    }

    public void setLowCount(Integer lowCount) {
        this.lowCount = lowCount;
    }

    public Integer getMediumCount() {
        return mediumCount;
    }

    public void setMediumCount(Integer mediumCount) {
        this.mediumCount = mediumCount;
    }

    public Integer getHighCount() {
        return highCount;
    }

    public void setHighCount(Integer highCount) {
        this.highCount = highCount;
    }

    public Integer getCriticalCount() {
        return criticalCount;
    }

    public void setCriticalCount(Integer criticalCount) {
        this.criticalCount = criticalCount;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

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
