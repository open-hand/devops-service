package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * polaris扫描结果的Deployment这一级别的数据
 *
 * @author zmf
 * @since 2/17/20
 */
@Table(name = "devops_polaris_instance_result")
public class DevopsPolarisInstanceResultDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("自增id")
    private Long id;

    @ApiModelProperty("环境id / 可为空")
    private Long envId;

    @ApiModelProperty("实例id / 可为空")
    private Long instanceId;

    @ApiModelProperty("集群namespace")
    private String namespace;

    @ApiModelProperty("资源名称")
    private String resourceName;

    @ApiModelProperty("资源类型")
    private String resourceKind;

    @ApiModelProperty("扫描纪录id")
    private Long recordId;

    @ApiModelProperty("此条资源详细扫描纪录id")
    private Long detailId;
}
