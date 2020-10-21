package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 节点检查进度
 */
public class DevopsNodeCheckResultVO {
    private String status;
    private Step configuration;
    private Step system;
    private Step memory;
    private Step cpu;

    public String getStatus() {
        return status;
    }

    public DevopsNodeCheckResultVO setStatus(String status) {
        this.status = status;
        return this;
    }

    public Step getConfiguration() {
        return configuration;
    }

    public DevopsNodeCheckResultVO setConfiguration(Step configuration) {
        this.configuration = configuration;
        return this;
    }

    public Step getSystem() {
        return system;
    }

    public DevopsNodeCheckResultVO setSystem(Step system) {
        this.system = system;
        return this;
    }

    public Step getMemory() {
        return memory;
    }

    public DevopsNodeCheckResultVO setMemory(Step memory) {
        this.memory = memory;
        return this;
    }

    public Step getCpu() {
        return cpu;
    }

    public DevopsNodeCheckResultVO setCpu(Step cpu) {
        this.cpu = cpu;
        return this;
    }
}

class Step {
    private String status;
    private String errorMessage;

    public String getStatus() {
        return status;
    }

    public Step setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Step setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}