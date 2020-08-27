package io.choerodon.devops.api.vo.rdupm;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/8/27 20:17
 */
public class ResponseVO {
    private Boolean failed;
    private String code;
    private String message;
    private String type;

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
