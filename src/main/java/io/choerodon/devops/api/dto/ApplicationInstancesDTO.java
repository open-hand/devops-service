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
    private String publishLevel;
    private String applicationCode;
    private Long latestVersionId;
    private String latestVersion;
    private Integer latestVersionRunning;
    private List<EnvInstancesDTO> instances;
    private List<EnvInstanceDTO> envInstances;

    public ApplicationInstancesDTO() {
    }

    /**
     * 构造函数
     */
    public ApplicationInstancesDTO(Long applicationId,
                                   String publishLevel,
                                   String applicationName,
                                   String applicationCode,
                                   Long latestVersionId,
                                   String latestVersion) {
        this.applicationId = applicationId;
        this.publishLevel = publishLevel;
        this.latestVersionId = latestVersionId;
        this.applicationName = applicationName;
        this.applicationCode = applicationCode;
        this.latestVersion = latestVersion;
        this.latestVersionRunning = 0;
        this.envInstances = new ArrayList<>();
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

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public List<EnvInstanceDTO> getEnvInstances() {
        return envInstances;
    }

    public void setEnvInstances(List<EnvInstanceDTO> envInstances) {
        this.envInstances = envInstances;
    }

    public EnvInstanceDTO queryLastEnvInstanceDTO() {
        return envInstances.get(envInstances.size() - 1);
    }

    public void appendEnvInstanceDTOS(EnvInstanceDTO envInstanceDTO) {
        this.envInstances.add(envInstanceDTO);
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

    public Long getLatestVersionId() {
        return latestVersionId;
    }

    public void setLatestVersionId(Long latestVersionId) {
        this.latestVersionId = latestVersionId;
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

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }
}
