package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.deploy.JarDeployVO;

public class DevopsDeployGroupContainerConfigVO {
    private String name;
    private String type;
    private String sourceType;
    private String jarFileDownloadUrl;
    private JarDeployVO jarDeployVO;
    private DevopsDeployGroupDockerConfigVO dockerDeployVO;
    private String requestCpu;
    private String requestMemory;
    private String limitCpu;
    private String limitMemory;
    private Map<String, String> envs;
    private List<Map<String, String>> ports;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getJarFileDownloadUrl() {
        return jarFileDownloadUrl;
    }

    public void setJarFileDownloadUrl(String jarFileDownloadUrl) {
        this.jarFileDownloadUrl = jarFileDownloadUrl;
    }

    public JarDeployVO getJarDeployVO() {
        return jarDeployVO;
    }

    public void setJarDeployVO(JarDeployVO jarDeployVO) {
        this.jarDeployVO = jarDeployVO;
    }

    public DevopsDeployGroupDockerConfigVO getDockerDeployVO() {
        return dockerDeployVO;
    }

    public void setDockerDeployVO(DevopsDeployGroupDockerConfigVO dockerDeployVO) {
        this.dockerDeployVO = dockerDeployVO;
    }

    public String getRequestCpu() {
        return requestCpu;
    }

    public void setRequestCpu(String requestCpu) {
        this.requestCpu = requestCpu;
    }

    public String getRequestMemory() {
        return requestMemory;
    }

    public void setRequestMemory(String requestMemory) {
        this.requestMemory = requestMemory;
    }

    public String getLimitCpu() {
        return limitCpu;
    }

    public void setLimitCpu(String limitCpu) {
        this.limitCpu = limitCpu;
    }

    public String getLimitMemory() {
        return limitMemory;
    }

    public void setLimitMemory(String limitMemory) {
        this.limitMemory = limitMemory;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, String> envs) {
        this.envs = envs;
    }

    public List<Map<String, String>> getPorts() {
        return ports;
    }

    public void setPorts(List<Map<String, String>> ports) {
        this.ports = ports;
    }
}