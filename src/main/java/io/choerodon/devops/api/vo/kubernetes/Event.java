package io.choerodon.devops.api.vo.kubernetes;

import io.swagger.annotations.ApiModelProperty;

public class Event {
    private Metadata metadata;
    private InvolvedObject involvedObject;
    private String message;
    @ApiModelProperty("实例事件所属的commit")
    private String commitSha;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public InvolvedObject getInvolvedObject() {
        return involvedObject;
    }

    public void setInvolvedObject(InvolvedObject involvedObject) {
        this.involvedObject = involvedObject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }
}