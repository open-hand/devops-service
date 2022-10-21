package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author lihao
 * @since 2022/07/22
 */
public class CheckInfoVO {
    private boolean success;
    private String errMsg;

    public CheckInfoVO() {
    }

    public CheckInfoVO(boolean success, String errMsg) {
        this.success = success;
        this.errMsg = errMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
