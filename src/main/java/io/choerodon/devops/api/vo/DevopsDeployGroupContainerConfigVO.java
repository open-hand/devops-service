package io.choerodon.devops.api.vo;

import java.util.Map;

import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;

public class DevopsDeployGroupContainerConfigVO {
    private String name;
    private String type;
    private String sourceType;
    private String jarFileDownloadUrl;
    private JarDeployVO jarDeployVO;
    private DockerDeployVO dockerDeployVO;
    private String requestCpu;
    private String requestMemory;
    private String limitCpu;
    private String limitMemory;
    private Map<String, String> envs;
    private Map<String, Integer> resources;

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

    public DockerDeployVO getDockerDeployVO() {
        return dockerDeployVO;
    }

    public void setDockerDeployVO(DockerDeployVO dockerDeployVO) {
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

    public Map<String, Integer> getResources() {
        return resources;
    }

    public void setResources(Map<String, Integer> resources) {
        this.resources = resources;
    }
}
