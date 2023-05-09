package io.choerodon.devops.infra.dto.gitlab;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.util.JacksonJsonEnumHelper;

public class CommitActionDTO {

    public static enum Action {

        CREATE, DELETE, MOVE, UPDATE, CHMOD;

        private static JacksonJsonEnumHelper<Action> enumHelper = new JacksonJsonEnumHelper<>(Action.class);

        @JsonCreator
        public static Action forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    @ApiModelProperty("对文件进行的操作 / 必要")
    private Action action;
    @ApiModelProperty("文件的路径 / 必要")
    private String filePath;
    @ApiModelProperty("原始完整路径文件被移动。例如lib / class1.rb。只针对Move")
    private String previousPath;
    @ApiModelProperty("文件的内容 / 必要")
    private String content;
    @ApiModelProperty("最后已知的文件提交id。将只考虑更新、移动和删除操作")
    private String lastCommitId;
    @ApiModelProperty("当真/假启用/禁用文件可执行的标志。只考虑chmod操作")
    private Boolean executeFilemode;

    public CommitActionDTO() {
    }

    public CommitActionDTO(Action action, String filePath, String content) {
        this.action = action;
        this.filePath = filePath;
        this.content = content;
    }

    public CommitActionDTO(Action action, String filePath, String content, String lastCommitId) {
        this.action = action;
        this.filePath = filePath;
        this.content = content;
        this.lastCommitId = lastCommitId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPreviousPath() {
        return previousPath;
    }

    public void setPreviousPath(String previousPath) {
        this.previousPath = previousPath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public Boolean getExecuteFilemode() {
        return executeFilemode;
    }

    public void setExecuteFilemode(Boolean executeFilemode) {
        this.executeFilemode = executeFilemode;
    }
}
