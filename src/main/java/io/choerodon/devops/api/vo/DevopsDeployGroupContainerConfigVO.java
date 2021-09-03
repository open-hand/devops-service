package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

public class DevopsDeployGroupContainerConfigVO {
    private String name;
    private String type;
    private String sourceType;
    private DevopsDeployGroupJarDeployVO jarDeployVO;
    private DevopsDeployGroupDockerDeployVO dockerDeployVO;
    private String pipelineJobName;
    private String requestCpu;
    private String requestMemory;
    private String limitCpu;
    private String limitMemory;
    private Map<String, String> envs;
    private List<Map<String, String>> ports;

    public String getPipelineJobName() {
        return pipelineJobName;
    }

    public void setPipelineJobName(String pipelineJobName) {
        this.pipelineJobName = pipelineJobName;
    }

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

    public DevopsDeployGroupJarDeployVO getJarDeployVO() {
        return jarDeployVO;
    }

    public void setJarDeployVO(DevopsDeployGroupJarDeployVO jarDeployVO) {
        this.jarDeployVO = jarDeployVO;
    }

    public DevopsDeployGroupDockerDeployVO getDockerDeployVO() {
        return dockerDeployVO;
    }

    public void setDockerDeployVO(DevopsDeployGroupDockerDeployVO dockerDeployVO) {
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
