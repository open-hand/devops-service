package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:00 2019/8/9
 * Description:
 */
public class PipelineSearchVO {
    private Boolean creator;
    private Boolean executor;
    private Boolean manager;
    private Long envId;
    private String triggerType;
    private Boolean projectOwner;

    public Boolean getCreator() {
        return creator;
    }

    public void setCreator(Boolean creator) {
        this.creator = creator;
    }

    public Boolean getExecutor() {
        return executor;
    }

    public void setExecutor(Boolean executor) {
        this.executor = executor;
    }

    public Boolean getManager() {
        return manager;
    }

    public void setManager(Boolean manager) {
        this.manager = manager;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }
}
