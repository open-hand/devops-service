package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:11 2019/5/27
 * Description:
 */
public class PipelineRecordSearchDTO {
    private Boolean checkPending;
    private Boolean reviewed;
    private Boolean executed;

    public PipelineRecordSearchDTO(Boolean checkPending, Boolean reviewed, Boolean executed) {
        this.checkPending = checkPending;
        this.reviewed = reviewed;
        this.executed = executed;
    }

    public Boolean getCheckPending() {
        return checkPending;
    }

    public void setCheckPending(Boolean checkPending) {
        this.checkPending = checkPending;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Boolean getExecuted() {
        return executed;
    }

    public void setExecuted(Boolean executed) {
        this.executed = executed;
    }
}
