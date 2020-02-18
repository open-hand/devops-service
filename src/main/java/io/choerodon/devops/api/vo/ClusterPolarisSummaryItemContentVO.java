package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/18/20
 */
public class ClusterPolarisSummaryItemContentVO {
    @ApiModelProperty("namespace")
    private String namespace;
    @ApiModelProperty("资源类型")
    private String resourceKind;
    @ApiModelProperty("资源名称")
    private String resourceName;
    @ApiModelProperty("检测项")
    private List<ClusterPolarisSummaryItemDetailVO> items;
    @ApiModelProperty("是否有error的检测项")
    private Boolean hasErrors;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getResourceKind() {
        return resourceKind;
    }

    public void setResourceKind(String resourceKind) {
        this.resourceKind = resourceKind;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public List<ClusterPolarisSummaryItemDetailVO> getItems() {
        return items;
    }

    public void setItems(List<ClusterPolarisSummaryItemDetailVO> items) {
        this.items = items;
    }

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}
