package io.choerodon.devops.api.vo;

/**
 * Created by younger on 2018/4/25.
 */
public class InstanceStageVO {

    private String stageName;
    private Long weight;
    private String status;
    private String log;
    private Long[] stageTime;

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long[] getStageTime() {
        return stageTime;
    }

    public void setStageTime(Long[] stageTime) {
        this.stageTime = stageTime;
    }
}
