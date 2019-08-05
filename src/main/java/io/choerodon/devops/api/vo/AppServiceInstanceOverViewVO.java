package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/4/18
 * Time: 20:55
 * Description:
 */
public class AppServiceInstanceOverViewVO {
    private Long appServiceId;
    private String appServiceName;
    private String publishLevel;
    private String appServiceCode;
    private Long latestVersionId;
    private String latestVersion;
    private Integer latestVersionRunning;
    private List<EnvInstancesVO> instances;
    private List<EnvInstanceVO> envInstances;
    private Long projectId;

    public AppServiceInstanceOverViewVO() {
    }

    /**
     * 构造函数
     */
    public AppServiceInstanceOverViewVO(Long appServiceId,
                                        String publishLevel,
                                        String appServiceName,
                                        String appServiceCode,
                                        Long latestVersionId,
                                        String latestVersion) {
        this.appServiceId = appServiceId;
        this.publishLevel = publishLevel;
        this.latestVersionId = latestVersionId;
        this.appServiceName = appServiceName;
        this.appServiceCode = appServiceCode;
        this.latestVersion = latestVersion;
        this.latestVersionRunning = 0;
        this.envInstances = new ArrayList<>();
        this.instances = new ArrayList<>();
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public List<EnvInstanceVO> getEnvInstances() {
        return envInstances;
    }

    public void setEnvInstances(List<EnvInstanceVO> envInstances) {
        this.envInstances = envInstances;
    }

    public EnvInstanceVO queryLastEnvInstanceVO() {
        return envInstances.get(envInstances.size() - 1);
    }

    public void appendEnvInstanceVOS(EnvInstanceVO envInstanceVO) {
        this.envInstances.add(envInstanceVO);
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

    public List<EnvInstancesVO> getInstances() {
        return instances;
    }

    public void setInstances(List<EnvInstancesVO> instances) {
        this.instances = instances;
    }

    public void appendInstances(EnvInstancesVO instancesDTO) {
        this.instances.add(instancesDTO);
    }

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
