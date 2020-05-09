package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotEmpty;

/**
 * Created by Sheep on 2019/5/14.
 */
//public class NotifyVO extends io.choerodon.core.notify.NoticeSendDTO {
public class NotifyVO {

    /**
     * 发送的业务类型code
     */
    @NotEmpty(message = "error.postNotify.codeEmpty")
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
