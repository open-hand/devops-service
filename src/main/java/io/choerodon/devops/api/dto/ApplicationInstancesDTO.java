package io.choerodon.devops.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/4/18
 * Time: 20:55
 * Description:
 */
public class ApplicationInstancesDTO {
    private Long applicationId;
    private String applicationName;
    private String applicationCode;
    private Long applicationLatestVersionId;
    private String applicationLatestVersion;
    private Integer latestVersionRunning;
    private List<EnvInstancesDTO> instances;
    private List<EnvInstanceDTO> envInstanceDTOS;

    public ApplicationInstancesDTO() {
    }

    /**
     * 构造函数
     */
    public ApplicationInstancesDTO(Long applicationId,
                                   String applicationName,
                                   String applicationCode,
                                   Long applicationLatestVersionId,
                                   String applicationLatestVersion) {
        this.applicationId = applicationId;
        this.applicationLatestVersionId = applicationLatestVersionId;
        this.applicationName = applicationName;
        this.applicationCode = applicationCode;
        this.applicationLatestVersion = applicationLatestVersion;
        this.latestVersionRunning = 0;
        this.envInstanceDTOS = new ArrayList<>();
        this.instances = new ArrayList<>();
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getApplicationLatestVersion() {
        return applicationLatestVersion;
    }

    public void setApplicationLatestVersion(String applicationLatestVersion) {
        this.applicationLatestVersion = applicationLatestVersion;
    }

    public List<EnvInstanceDTO> getEnvInstanceDTOS() {
        return envInstanceDTOS;
    }

    public void setEnvInstanceDTOS(List<EnvInstanceDTO> envInstanceDTOS) {
        this.envInstanceDTOS = envInstanceDTOS;
    }

    public EnvInstanceDTO queryLastEnvInstanceDTO() {
        return envInstanceDTOS.get(envInstanceDTOS.size() - 1);
    }

    public void appendEnvInstanceDTOS(EnvInstanceDTO envInstanceDTO) {
        this.envInstanceDTOS.add(envInstanceDTO);
    }

    public Integer getLatestVersionRunning() {
        return latestVersionRunning;
    }

    public void setLatestVersionRunning(Integer latestVersionRunning) {
        this.latestVersionRunning = latestVersionRunning;
    }

    public void addLatestVersionRunning() {
        this.latestVersionRunning += 1;
    }

    public Long getApplicationLatestVersionId() {
        return applicationLatestVersionId;
    }

    public void setApplicationLatestVersionId(Long applicationLatestVersionId) {
        this.applicationLatestVersionId = applicationLatestVersionId;
    }

    public List<EnvInstancesDTO> getInstances() {
        return instances;
    }

    public void setInstances(List<EnvInstancesDTO> instances) {
        this.instances = instances;
    }

    public void appendInstances(EnvInstancesDTO instancesDTO) {
        this.instances.add(instancesDTO);
    }

}
