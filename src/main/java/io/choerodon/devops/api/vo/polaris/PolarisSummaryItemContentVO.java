package io.choerodon.devops.api.vo.polaris;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/18/20
 */
public class PolarisSummaryItemContentVO {
    @ApiModelProperty("namespace")
    private String namespace;
    @ApiModelProperty("资源类型")
    private String resourceKind;
    @ApiModelProperty("资源名称")
    private String resourceName;
    @ApiModelProperty("检测项")
    private List<PolarisSummaryItemDetailVO> items;
    @ApiModelProperty("是否有error的检测项")
    private Boolean hasErrors;

    public PolarisSummaryItemContentVO() {
    }

    public PolarisSummaryItemContentVO(String namespace, String resourceKind, String resourceName, List<PolarisSummaryItemDetailVO> items, Boolean hasErrors) {
        this.namespace = namespace;
        this.resourceKind = resourceKind;
        this.resourceName = resourceName;
        this.items = items;
        this.hasErrors = hasErrors;
    }

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

    public List<PolarisSummaryItemDetailVO> getItems() {
        return items;
    }

    public void setItems(List<PolarisSummaryItemDetailVO> items) {
        this.items = items;
    }

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}
