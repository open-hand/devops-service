package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/4/18
 * Time: 21:11
 * Description:
 */
public class EnvVersionDTO {
    private Long versionId;
    private String version;
    private Integer runningCount;
    private Boolean isLatest;
    private List<EnvInstancesDTO> instances;

    public EnvVersionDTO() {
    }

    /**
     * 构造函数
     */
    public EnvVersionDTO(Long versionId, String version, Long instanceId, String instanceName, String instanceStatus) {
        this.versionId = versionId;
        this.version = version;
        this.runningCount = 1;
        this.instances = new ArrayList<>();
        this.instances.add(new EnvInstancesDTO(instanceId, instanceName, instanceStatus));
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(Integer runningCount) {
        this.runningCount = runningCount;
    }

    public List<EnvInstancesDTO> getInstances() {
        return instances;
    }

    public void setInstances(List<EnvInstancesDTO> instances) {
        this.instances = instances;
    }

    public void appendInstanceList(Long instanceId, String instanceName, String instanceStatus) {
        this.instances.add(new EnvInstancesDTO(instanceId, instanceName, instanceStatus));
        this.runningCount += 1;
    }

    public Boolean getLatest() {
        return isLatest;
    }

    public void setLatest(Boolean latest) {
        isLatest = latest;
    }
}
