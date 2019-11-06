package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


public class DevopsPvReqVo {

    @ApiModelProperty("pvId")
    private Long id;

    @ApiModelProperty("pv名称")
    @Pattern(regexp = "[a-z]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*", message = "error.pv.name.pattern")
    @Length(max = 40, min = 1, message = "error.pv.name.length")
    private String name;

    @NotNull(message = "error.pv.type.is.null")
    @ApiModelProperty("pv类型")
    private String type;

    @NotNull(message = "error.pv.description.is.null")
    @ApiModelProperty("pv描述")
    private String description;

    @NotNull(message = "error.pv.related.pvc.is.null")
    @ApiModelProperty("关联的pvcId")
    private Long pvcId;

    @NotNull(message = "error.pv.related.cluster.is.null")
    @ApiModelProperty("关联的集群Id")
    private Long clusterId;

    @NotNull(message = "error.pv.accessmodes.is.null")
    @ApiModelProperty("访问模式")
    private String accessModes;

    @NotNull(message = "error.pv.checkpermission.is.null")
    @ApiModelProperty("是否跳过权限校验，默认为true")
    private Boolean skipCheckProjectPermission;

    @NotNull(message = "error.pv.requestResource.is.null")
    @ApiModelProperty("资源大小")
    private String requestResource;
}
