package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class DevopsPvcRespVO extends DevopsResourceDataInfoVO {
    @ApiModelProperty("PVC id")
    private Long id;

    @ApiModelProperty("PVC名称")
    private String name;

    @ApiModelProperty("PVC绑定环境ID")
    private Long envId;

    @ApiModelProperty("PVC绑定PV id")
    private Long pvId;

    @ApiModelProperty("访问模式")
    private String accessModes;

    @ApiModelProperty("资源申请数量")
    private String requestResource;

    @ApiModelProperty("卷类型")
    private String type;
}
