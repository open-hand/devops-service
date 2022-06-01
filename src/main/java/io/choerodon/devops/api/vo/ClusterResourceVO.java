package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

/**
 * @author: 25499
 * @date: 2019/10/30 13:59
 * @description:
 */
public class ClusterResourceVO {
    @ApiModelProperty("组件状态")
    private String status;
    @ApiParam(value = "错误消息")
    private String message;
    @ApiParam(value = "组件类型")
    private String type;
    @ApiParam(value = "操作")
    private String operate;

    public ClusterResourceVO() {
    }

    public ClusterResourceVO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }
}
