package io.choerodon.devops.api.vo.host;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 21:54
 */
public class CommandResultVO {
    private Boolean success;
    private String payload;
    private String errorMsg;

    private Long commandId;
    /**
     * 用于标识同步的命令，agent是否处理，true表示agent未处理该命令
     */
    private Boolean notExist;

    public Boolean getNotExist() {
        return notExist;
    }

    public void setNotExist(Boolean notExist) {
        this.notExist = notExist;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
