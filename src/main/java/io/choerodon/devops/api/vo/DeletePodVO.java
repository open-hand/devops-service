package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 删除Pod所需要的信息
 *
 * @author zmf
 * @since 11/19/19
 */
public class DeletePodVO {
    @ApiModelProperty("pod的name")
    private String podName;
    @ApiModelProperty("pod所在的namespace")
    private String namespace;
    @ApiModelProperty("执行状态")
    private String status;

    public DeletePodVO() {
    }

    public DeletePodVO(String podName, String namespace) {
        this.podName = podName;
        this.namespace = namespace;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DeletePodVO{" +
                "podName='" + podName + '\'' +
                ", namespace='" + namespace + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
